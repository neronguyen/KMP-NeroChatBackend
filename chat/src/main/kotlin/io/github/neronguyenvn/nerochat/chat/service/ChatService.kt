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
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId.asUUID(),
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.asExternalModel().asDto() }
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

        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId.asUUID())
            ?: throw ChatParticipantNotFoundException(creatorId)

        val savedChat = chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = otherParticipants
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
                userIds = userIds
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
                userId = userId
            )
        )
    }

    private fun findLastMessageOfChat(chatId: ChatId): ChatMessage {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId.asUUID()))
            .first()
            .asExternalModel()
    }
}
