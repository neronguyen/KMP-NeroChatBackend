package io.github.neronguyenvn.nerochat.chat.service

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatParticipant
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatParticipantEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatParticipantRepository
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class ChatParticipantServiceTest {

    private lateinit var chatParticipantRepository: ChatParticipantRepository
    private lateinit var chatParticipantService: ChatParticipantService

    private val userId = UserId(UUID.randomUUID())
    private val email = "User.Name@Example.com"
    private val normalizedEmail = "user.name@example.com"
    private val displayName = "User Name"
    private val profilePictureUrl = "https://example.com/avatar.png"

    @BeforeEach
    fun setUp() {
        chatParticipantRepository = mock(ChatParticipantRepository::class.java)
        chatParticipantService = ChatParticipantService(chatParticipantRepository)
    }

    @Test
    fun `createChatParticipant saves participant with normalized email and profile picture url`() {
        val chatParticipant = ChatParticipant(
            userId = userId,
            displayName = displayName,
            email = email,
            profilePictureUrl = profilePictureUrl
        )

        chatParticipantService.createChatParticipant(chatParticipant)

        val captor = ArgumentCaptor.forClass(ChatParticipantEntity::class.java)
        verify(chatParticipantRepository).save(captor.capture())

        val savedEntity = captor.value
        assertEquals(UUID.fromString(userId.value), savedEntity.userId)
        assertEquals(normalizedEmail, savedEntity.email)
        assertEquals(displayName, savedEntity.displayName)
        assertEquals(profilePictureUrl, savedEntity.profilePictureUrl)
    }

    @Test
    fun `findChatParticipantById returns participant when found`() {
        val uuid = UUID.fromString(userId.value)
        val entity = ChatParticipantEntity(
            userId = uuid,
            email = normalizedEmail,
            displayName = displayName,
            profilePictureUrl = profilePictureUrl
        )
        `when`(chatParticipantRepository.findById(uuid)).thenReturn(Optional.of(entity))

        val result = chatParticipantService.findChatParticipantById(userId)

        assertNotNull(result)
        assertEquals(userId, result?.userId)
        assertEquals(normalizedEmail, result?.email)
        assertEquals(displayName, result?.displayName)
        assertEquals(profilePictureUrl, result?.profilePictureUrl)
        verify(chatParticipantRepository).findById(uuid)
    }

    @Test
    fun `findChatParticipantById returns null when not found`() {
        val uuid = UUID.fromString(userId.value)
        `when`(chatParticipantRepository.findById(uuid)).thenReturn(Optional.empty())

        val result = chatParticipantService.findChatParticipantById(userId)

        assertNull(result)
        verify(chatParticipantRepository).findById(uuid)
    }

    @Test
    fun `findChatParticipantByEmail returns participant when found with normalized email`() {
        val uuid = UUID.fromString(userId.value)
        val entity = ChatParticipantEntity(
            userId = uuid,
            email = normalizedEmail,
            displayName = displayName,
            profilePictureUrl = profilePictureUrl
        )
        `when`(chatParticipantRepository.findByEmail(normalizedEmail)).thenReturn(entity)

        val result = chatParticipantService.findChatParticipantByEmail(email)

        assertNotNull(result)
        assertEquals(userId, result?.userId)
        assertEquals(normalizedEmail, result?.email)
        assertEquals(displayName, result?.displayName)
        assertEquals(profilePictureUrl, result?.profilePictureUrl)
        verify(chatParticipantRepository).findByEmail(normalizedEmail)
    }

    @Test
    fun `findChatParticipantByEmail returns null when not found`() {
        `when`(chatParticipantRepository.findByEmail(normalizedEmail)).thenReturn(null)

        val result = chatParticipantService.findChatParticipantByEmail(email)

        assertNull(result)
        verify(chatParticipantRepository).findByEmail(normalizedEmail)
    }
}
