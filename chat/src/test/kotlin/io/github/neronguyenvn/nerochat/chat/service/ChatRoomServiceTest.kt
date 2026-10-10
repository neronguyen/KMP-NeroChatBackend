package io.github.neronguyenvn.nerochat.chat.service

import io.github.neronguyenvn.nerochat.chat.domain.exception.ChatParticipantNotFoundException
import io.github.neronguyenvn.nerochat.chat.domain.exception.InvalidChatRoomSizeException
import io.github.neronguyenvn.nerochat.chat.domain.model.ChatRoomType
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatMessageEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatParticipantEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatRoomEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatMessageRepository
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatParticipantRepository
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatRoomRepository
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ChatRoomServiceTest {

    private lateinit var chatParticipantRepository: ChatParticipantRepository
    private lateinit var chatRoomRepository: ChatRoomRepository
    private lateinit var chatMessageRepository: ChatMessageRepository
    private lateinit var applicationEventPublisher: ApplicationEventPublisher
    private lateinit var chatRoomService: ChatRoomService

    private val creatorId = UserId(UUID.randomUUID())
    private val targetUserId = UserId(UUID.randomUUID())
    private val user3Id = UserId(UUID.randomUUID())

    private lateinit var creatorEntity: ChatParticipantEntity
    private lateinit var targetEntity: ChatParticipantEntity
    private lateinit var user3Entity: ChatParticipantEntity

    @BeforeEach
    fun setUp() {
        chatParticipantRepository = mock(ChatParticipantRepository::class.java)
        chatRoomRepository = mock(ChatRoomRepository::class.java)
        chatMessageRepository = mock(ChatMessageRepository::class.java)
        applicationEventPublisher = mock(ApplicationEventPublisher::class.java)

        chatRoomService = ChatRoomService(
            chatParticipantRepository = chatParticipantRepository,
            chatRoomRepository = chatRoomRepository,
            chatMessageRepository = chatMessageRepository,
            applicationEventPublisher = applicationEventPublisher,
        )

        creatorEntity = ChatParticipantEntity(
            userId = creatorId.asUUID(),
            email = "creator@example.com",
            displayName = "Creator User"
        )
        targetEntity = ChatParticipantEntity(
            userId = targetUserId.asUUID(),
            email = "target@example.com",
            displayName = "Target User"
        )
        user3Entity = ChatParticipantEntity(
            userId = user3Id.asUUID(),
            email = "user3@example.com",
            displayName = "User 3"
        )
    }

    @Test
    fun `creates direct chat room when not existing`() {
        `when`(chatParticipantRepository.findById(creatorId.asUUID())).thenReturn(Optional.of(creatorEntity))
        `when`(chatParticipantRepository.findById(targetUserId.asUUID())).thenReturn(Optional.of(targetEntity))
        `when`(
            chatRoomRepository.findDirectChatRoomBetween(
                creatorId.asUUID(),
                targetUserId.asUUID(),
                ChatRoomType.DIRECT
            )
        )
            .thenReturn(null)

        val roomId = UUID.randomUUID()
        val createdRoomEntity = ChatRoomEntity(
            id = roomId,
            type = ChatRoomType.DIRECT,
            creator = creatorEntity,
            participants = setOf(creatorEntity, targetEntity),
            createdAt = Instant.now()
        )
        `when`(chatRoomRepository.save(any(ChatRoomEntity::class.java))).thenReturn(createdRoomEntity)

        val messageId = UUID.randomUUID()
        val createdMessageEntity = ChatMessageEntity(
            id = messageId,
            chatRoomId = roomId,
            sender = creatorEntity,
            content = "Hello there",
            createdAt = Instant.now()
        )
        `when`(chatMessageRepository.save(any(ChatMessageEntity::class.java))).thenReturn(createdMessageEntity)

        val result = chatRoomService.createDirectChatRoom(
            creatorId = creatorId,
            targetUserId = targetUserId,
            message = "Hello there"
        )

        assertNotNull(result)
        assertEquals(roomId.toString(), result.id.value)
        assertEquals(ChatRoomType.DIRECT, result.type)
        assertNull(result.name)
        assertEquals(creatorId.value, result.creator.userId.value)
        assertEquals(2, result.participants.size)
        assertNotNull(result.lastMessage)
        assertEquals("Hello there", result.lastMessage?.content)

        verify(chatRoomRepository).save(any(ChatRoomEntity::class.java))
        verify(chatMessageRepository).save(any(ChatMessageEntity::class.java))
    }

    @Test
    fun `idempotently returns existing direct chat room without creating a new one`() {
        `when`(chatParticipantRepository.findById(creatorId.asUUID())).thenReturn(Optional.of(creatorEntity))
        `when`(chatParticipantRepository.findById(targetUserId.asUUID())).thenReturn(Optional.of(targetEntity))

        val existingRoomId = UUID.randomUUID()
        val existingRoomEntity = ChatRoomEntity(
            id = existingRoomId,
            type = ChatRoomType.DIRECT,
            creator = creatorEntity,
            participants = setOf(creatorEntity, targetEntity),
            createdAt = Instant.now()
        )
        `when`(
            chatRoomRepository.findDirectChatRoomBetween(
                creatorId.asUUID(),
                targetUserId.asUUID(),
                ChatRoomType.DIRECT
            )
        )
            .thenReturn(existingRoomEntity)

        val existingMessageEntity = ChatMessageEntity(
            id = UUID.randomUUID(),
            chatRoomId = existingRoomId,
            sender = creatorEntity,
            content = "Previous message",
            createdAt = Instant.now()
        )
        `when`(chatMessageRepository.findLatestMessagesByChatRoomIds(setOf(existingRoomId)))
            .thenReturn(listOf(existingMessageEntity))

        val result = chatRoomService.createDirectChatRoom(
            creatorId = creatorId,
            targetUserId = targetUserId,
            message = "Some message"
        )

        assertEquals(existingRoomId.toString(), result.id.value)
        assertEquals(ChatRoomType.DIRECT, result.type)
        assertEquals("Previous message", result.lastMessage?.content)

        verify(chatRoomRepository, never()).save(any(ChatRoomEntity::class.java))
        verify(chatMessageRepository, never()).save(any(ChatMessageEntity::class.java))
    }

    @Test
    fun `throws InvalidChatRoomSizeException when creatorId equals targetUserId`() {
        assertThrows<InvalidChatRoomSizeException> {
            chatRoomService.createDirectChatRoom(
                creatorId = creatorId,
                targetUserId = creatorId,
                message = "Hello"
            )
        }
    }

    @Test
    fun `createDirectChatRoom throws ChatParticipantNotFoundException when creator does not exist`() {
        `when`(chatParticipantRepository.findById(creatorId.asUUID())).thenReturn(Optional.empty())

        assertThrows<ChatParticipantNotFoundException> {
            chatRoomService.createDirectChatRoom(
                creatorId = creatorId,
                targetUserId = targetUserId,
                message = "Hello"
            )
        }
    }

    @Test
    fun `createDirectChatRoom throws ChatParticipantNotFoundException when target user does not exist`() {
        `when`(chatParticipantRepository.findById(creatorId.asUUID())).thenReturn(Optional.of(creatorEntity))
        `when`(chatParticipantRepository.findById(targetUserId.asUUID())).thenReturn(Optional.empty())

        assertThrows<ChatParticipantNotFoundException> {
            chatRoomService.createDirectChatRoom(
                creatorId = creatorId,
                targetUserId = targetUserId,
                message = "Hello"
            )
        }
    }

    @Test
    fun `creates group chat room successfully with null lastMessage`() {
        `when`(chatParticipantRepository.findById(creatorId.asUUID())).thenReturn(Optional.of(creatorEntity))
        `when`(chatParticipantRepository.findByUserIdIn(setOf(targetUserId.asUUID(), user3Id.asUUID())))
            .thenReturn(setOf(targetEntity, user3Entity))

        val roomId = UUID.randomUUID()
        val createdGroupEntity = ChatRoomEntity(
            id = roomId,
            type = ChatRoomType.GROUP,
            name = "Project Team",
            creator = creatorEntity,
            participants = setOf(creatorEntity, targetEntity, user3Entity),
            createdAt = Instant.now()
        )
        `when`(chatRoomRepository.save(any(ChatRoomEntity::class.java))).thenReturn(createdGroupEntity)

        val result = chatRoomService.createGroupChatRoom(
            creatorId = creatorId,
            name = "Project Team",
            participantIds = listOf(targetUserId, user3Id)
        )

        assertNotNull(result)
        assertEquals(roomId.toString(), result.id.value)
        assertEquals(ChatRoomType.GROUP, result.type)
        assertEquals("Project Team", result.name)
        assertEquals(creatorId.value, result.creator.userId.value)
        assertEquals(3, result.participants.size)
        assertNull(result.lastMessage)

        verify(chatRoomRepository).save(any(ChatRoomEntity::class.java))
    }

    @Test
    fun `createGroupChatRoom throws InvalidChatRoomSizeException when fewer than 2 participants provided`() {
        assertThrows<InvalidChatRoomSizeException> {
            chatRoomService.createGroupChatRoom(
                creatorId = creatorId,
                name = "Group",
                participantIds = listOf(targetUserId)
            )
        }

        assertThrows<InvalidChatRoomSizeException> {
            chatRoomService.createGroupChatRoom(
                creatorId = creatorId,
                name = "Group",
                participantIds = emptyList()
            )
        }
    }

    @Test
    fun `createGroupChatRoom throws InvalidChatRoomSizeException when duplicate participants result in less than 2 unique`() {
        assertThrows<InvalidChatRoomSizeException> {
            chatRoomService.createGroupChatRoom(
                creatorId = creatorId,
                name = "Group",
                participantIds = listOf(targetUserId, targetUserId)
            )
        }
    }

    @Test
    fun `createGroupChatRoom throws ChatParticipantNotFoundException when creator does not exist`() {
        `when`(chatParticipantRepository.findById(creatorId.asUUID())).thenReturn(Optional.empty())

        assertThrows<ChatParticipantNotFoundException> {
            chatRoomService.createGroupChatRoom(
                creatorId = creatorId,
                name = "Group",
                participantIds = listOf(targetUserId, user3Id)
            )
        }
    }

    @Test
    fun `createGroupChatRoom throws ChatParticipantNotFoundException when a participant does not exist`() {
        `when`(chatParticipantRepository.findById(creatorId.asUUID())).thenReturn(Optional.of(creatorEntity))
        `when`(chatParticipantRepository.findByUserIdIn(setOf(targetUserId.asUUID(), user3Id.asUUID())))
            .thenReturn(setOf(targetEntity))

        assertThrows<ChatParticipantNotFoundException> {
            chatRoomService.createGroupChatRoom(
                creatorId = creatorId,
                name = "Group",
                participantIds = listOf(targetUserId, user3Id)
            )
        }
    }
}
