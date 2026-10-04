package io.github.neronguyenvn.nerochat.chat.service

import io.github.neronguyenvn.nerochat.chat.api.model.ChatMessageDto
import io.github.neronguyenvn.nerochat.chat.api.model.asDto
import io.github.neronguyenvn.nerochat.chat.domain.event.InternalChatEvent
import io.github.neronguyenvn.nerochat.chat.domain.exception.ChatNotFoundException
import io.github.neronguyenvn.nerochat.chat.domain.exception.ChatParticipantNotFoundException
import io.github.neronguyenvn.nerochat.chat.domain.exception.InvalidChatSizeException
import io.github.neronguyenvn.nerochat.chat.domain.model.Chat
import io.github.neronguyenvn.nerochat.chat.domain.model.ChatMessage
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatMessageEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatMessageRepository
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatParticipantRepository
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatRepository
import io.github.neronguyenvn.nerochat.domain.exception.ForbiddenException
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.infra.caching.CacheNames
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Service
class ChatService(
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Cacheable(
        value = [CacheNames.MESSAGES],
        key = "#chatId",
        condition = "#before == null",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
    ): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId.asUUID(),
                before = before,
                pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE)
            )
            .content
            .asReversed()
            .map { it.asExternalModel().asDto() }
    }

    fun getChatById(
        chatId: ChatId,
        requesterId: UserId
    ): Chat? {
        return chatRepository
            .findChatById(chatId.asUUID(), requesterId.asUUID())
            ?.asExternalModel(lastMessage = findLastMessageOfChat(chatId = chatId))
    }

    fun findChatsByUser(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId.asUUID())
        val chatIds = chatEntities.mapNotNull { it.id }.toSet()

        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds)
            .associateBy { it.chatId }

        return chatEntities
            .map { chatEntity ->
                val lastMessage = latestMessages[chatEntity.id] ?: error("Chat ${chatEntity.id} has no last message")
                chatEntity.asExternalModel(lastMessage = lastMessage.asExternalModel())
            }
            .sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,
        messageContent: String,
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds.map { it.asUUID() }.toSet()
        )

        if (otherParticipants.size != otherUserIds.size) {
            val foundIds = otherParticipants.map { UserId(it.userId) }.toSet()
            val missingIds = otherUserIds - foundIds
            throw ChatParticipantNotFoundException(missingIds.first())
        }

        val participantCount = otherParticipants.size + 1
        if (participantCount < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId.asUUID())
            ?: throw ChatParticipantNotFoundException(creatorId)

        val participants = setOf(creator) + otherParticipants

        val savedChat = chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = participants
            )
        )

        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                chatId = savedChat.id ?: error("ChatId have to be generated"),
                sender = creator,
                content = messageContent
            )
        )

        return savedChat.asExternalModel(savedMessage.asExternalModel())
    }

    @Transactional
    fun addParticipantsToChat(
        chatId: ChatId,
        requesterId: UserId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId.asUUID())
            ?: throw ChatNotFoundException()

        val isRequesterInChat = chat.participants.any {
            it.userId == requesterId.asUUID()
        }

        if (!isRequesterInChat) {
            throw ForbiddenException()
        }

        val chatParticipantIds = chat.participants.map { UserId(it.userId) }.toSet()

        val users = userIds.map { userId ->
            if (userId in chatParticipantIds) {
                throw ForbiddenException()
            }

            chatParticipantRepository.findByIdOrNull(userId.asUUID())
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = findLastMessageOfChat(chatId = chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).asExternalModel(lastMessage = lastMessage)

        applicationEventPublisher.publishEvent(
            InternalChatEvent.ChatParticipantJoinedEvent(
                chatId = chatId,
                newUsers = users.map { it.asExternalModel() }.toSet()
            )
        )

        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(chatId.asUUID())
            ?: throw ChatNotFoundException()

        val participant = chat.participants.find { UserId(it.userId) == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantsSize = chat.participants.size - 1
        if (newParticipantsSize == 0) {
            chatRepository.deleteById(chatId.asUUID())
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )

        applicationEventPublisher.publishEvent(
            InternalChatEvent.ChatParticipantLeftEvent(
                chatId = chatId,
                leftUser = participant.asExternalModel()
            )
        )
    }

    private fun findLastMessageOfChat(chatId: ChatId): ChatMessage {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId.asUUID()))
            .first()
            .asExternalModel()
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }
}
