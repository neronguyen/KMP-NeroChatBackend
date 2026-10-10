package io.github.neronguyenvn.nerochat.chat.service

import io.github.neronguyenvn.nerochat.chat.api.model.ChatMessageDto
import io.github.neronguyenvn.nerochat.chat.api.model.asDto
import io.github.neronguyenvn.nerochat.chat.domain.event.InternalChatEvent
import io.github.neronguyenvn.nerochat.chat.domain.exception.ChatParticipantNotFoundException
import io.github.neronguyenvn.nerochat.chat.domain.exception.ChatRoomNotFoundException
import io.github.neronguyenvn.nerochat.chat.domain.exception.InvalidChatRoomSizeException
import io.github.neronguyenvn.nerochat.chat.domain.model.ChatMessage
import io.github.neronguyenvn.nerochat.chat.domain.model.ChatRoom
import io.github.neronguyenvn.nerochat.chat.domain.model.ChatRoomType
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatMessageEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatRoomEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatMessageRepository
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatParticipantRepository
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatRoomRepository
import io.github.neronguyenvn.nerochat.domain.exception.ForbiddenException
import io.github.neronguyenvn.nerochat.domain.type.ChatRoomId
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
class ChatRoomService(
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Cacheable(
        value = [CacheNames.MESSAGES],
        key = "#chatRoomId",
        condition = "#before == null",
        sync = true
    )
    fun getChatMessages(
        chatRoomId: ChatRoomId,
        before: Instant?,
    ): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatRoomIdBefore(
                chatRoomId = chatRoomId.asUUID(),
                before = before,
                pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE)
            )
            .content
            .asReversed()
            .map { it.asExternalModel().asDto() }
    }

    fun getChatRoomById(
        chatRoomId: ChatRoomId,
        requesterId: UserId
    ): ChatRoom? {
        return chatRoomRepository
            .findChatRoomById(chatRoomId.asUUID(), requesterId.asUUID())
            ?.asExternalModel(lastMessage = findLastMessageOfChatRoom(chatRoomId = chatRoomId))
    }

    fun findChatRoomsByUser(userId: UserId): List<ChatRoom> {
        val chatRoomEntities = chatRoomRepository.findAllByUserId(userId.asUUID())
        val chatRoomIds = chatRoomEntities.mapNotNull { it.id }.toSet()

        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatRoomIds(chatRoomIds)
            .associateBy { it.chatRoomId }

        return chatRoomEntities
            .map { chatRoomEntity ->
                val lastMessage = latestMessages[chatRoomEntity.id]?.asExternalModel()
                chatRoomEntity.asExternalModel(lastMessage = lastMessage)
            }
            .sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createDirectChatRoom(
        creatorId: UserId,
        targetUserId: UserId,
        message: String,
    ): ChatRoom {
        if (creatorId == targetUserId) {
            throw InvalidChatRoomSizeException("Cannot create a direct chat room with yourself")
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId.asUUID())
            ?: throw ChatParticipantNotFoundException(creatorId)

        val target = chatParticipantRepository.findByIdOrNull(targetUserId.asUUID())
            ?: throw ChatParticipantNotFoundException(targetUserId)

        val existingRoom = chatRoomRepository.findDirectChatRoomBetween(
            userId1 = creatorId.asUUID(),
            userId2 = targetUserId.asUUID(),
        )

        if (existingRoom != null) {
            val lastMessage = chatMessageRepository
                .findLatestMessagesByChatRoomIds(setOf(existingRoom.id!!))
                .firstOrNull()
                ?.asExternalModel()

            return existingRoom.asExternalModel(lastMessage = lastMessage)
        }

        val newRoom = chatRoomRepository.save(
            ChatRoomEntity(
                type = ChatRoomType.DIRECT,
                creator = creator,
                participants = setOf(creator, target),
            )
        )

        val initialMessage = chatMessageRepository.save(
            ChatMessageEntity(
                chatRoomId = newRoom.id ?: error("ChatRoomId must be generated"),
                sender = creator,
                content = message.trim(),
            )
        )

        return newRoom.asExternalModel(lastMessage = initialMessage.asExternalModel())
    }

    @Transactional
    fun createGroupChatRoom(
        creatorId: UserId,
        name: String,
        participantIds: List<UserId>,
    ): ChatRoom {
        val uniqueParticipantIds = participantIds.toSet()
        val otherParticipantIds = uniqueParticipantIds - creatorId
        if (otherParticipantIds.size < 2) {
            throw InvalidChatRoomSizeException("Group chat rooms must have at least 2 other participants")
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId.asUUID())
            ?: throw ChatParticipantNotFoundException(creatorId)

        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            otherParticipantIds.map { it.asUUID() }.toSet()
        )

        if (otherParticipants.size != otherParticipantIds.size) {
            val foundIds = otherParticipants.map { UserId(it.userId) }.toSet()
            val missingIds = otherParticipantIds - foundIds
            throw ChatParticipantNotFoundException(missingIds.first())
        }

        val allParticipants = setOf(creator) + otherParticipants

        val groupRoom = chatRoomRepository.save(
            ChatRoomEntity(
                type = ChatRoomType.GROUP,
                name = name.trim(),
                creator = creator,
                participants = allParticipants,
            )
        )

        return groupRoom.asExternalModel(lastMessage = null)
    }

    @Transactional
    fun addParticipantsToChatRoom(
        chatRoomId: ChatRoomId,
        requesterId: UserId,
        userIds: Set<UserId>
    ): ChatRoom {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId.asUUID())
            ?: throw ChatRoomNotFoundException()

        if (chatRoom.type == ChatRoomType.DIRECT) {
            throw ForbiddenException()
        }

        val isRequesterInChatRoom = chatRoom.participants.any {
            it.userId == requesterId.asUUID()
        }

        if (!isRequesterInChatRoom) {
            throw ForbiddenException()
        }

        val chatParticipantIds = chatRoom.participants.map { UserId(it.userId) }.toSet()

        val users = userIds.map { userId ->
            if (userId in chatParticipantIds) {
                throw ForbiddenException()
            }

            chatParticipantRepository.findByIdOrNull(userId.asUUID())
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = findLastMessageOfChatRoom(chatRoomId = chatRoomId)
        val updatedChatRoom = chatRoomRepository.save(
            chatRoom.apply {
                this.participants = chatRoom.participants + users
            }
        ).asExternalModel(lastMessage = lastMessage)

        applicationEventPublisher.publishEvent(
            InternalChatEvent.ChatParticipantJoinedEvent(
                chatRoomId = chatRoomId,
                newUsers = users.map { it.asExternalModel() }.toSet()
            )
        )

        return updatedChatRoom
    }

    @Transactional
    fun removeParticipantFromChatRoom(
        chatRoomId: ChatRoomId,
        userId: UserId
    ) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId.asUUID())
            ?: throw ChatRoomNotFoundException()

        val participant = chatRoom.participants.find { UserId(it.userId) == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantsSize = chatRoom.participants.size - 1
        if (chatRoom.type == ChatRoomType.DIRECT || newParticipantsSize == 0) {
            chatRoomRepository.deleteById(chatRoomId.asUUID())
        } else {
            chatRoomRepository.save(
                chatRoom.apply {
                    this.participants = chatRoom.participants - participant
                }
            )
        }

        applicationEventPublisher.publishEvent(
            InternalChatEvent.ChatParticipantLeftEvent(
                chatRoomId = chatRoomId,
                leftUser = participant.asExternalModel()
            )
        )
    }

    private fun findLastMessageOfChatRoom(chatRoomId: ChatRoomId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatRoomIds(setOf(chatRoomId.asUUID()))
            .firstOrNull()
            ?.asExternalModel()
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }
}
