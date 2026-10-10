package io.github.neronguyenvn.nerochat.chat.api.controller

import io.github.neronguyenvn.nerochat.chat.api.model.ChatMessageDto
import io.github.neronguyenvn.nerochat.chat.domain.model.ChatRoom
import io.github.neronguyenvn.nerochat.chat.service.ChatRoomService
import io.github.neronguyenvn.nerochat.domain.exception.ForbiddenException
import io.github.neronguyenvn.nerochat.domain.type.ChatRoomId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ChatRoomControllerTest {

    private lateinit var chatRoomService: ChatRoomService
    private lateinit var controller: ChatRoomController

    private val userId = UserId(UUID.randomUUID())
    private val chatRoomId = ChatRoomId(UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        chatRoomService = mock(ChatRoomService::class.java)
        controller = ChatRoomController(chatRoomService)

        val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getMessagesForChatRoom returns messages when requester is participant`() {
        val chatRoom = mock(ChatRoom::class.java)
        `when`(chatRoomService.getChatRoomById(chatRoomId, userId)).thenReturn(chatRoom)

        val expectedMessages = listOf(
            ChatMessageDto(
                id = UUID.randomUUID().toString(),
                chatRoomId = chatRoomId.value,
                senderId = userId.value,
                content = "Hello",
                createdAt = Clock.System.now()
            )
        )
        `when`(chatRoomService.getChatMessages(chatRoomId, null)).thenReturn(expectedMessages)

        val result = controller.getMessagesForChatRoom(chatRoomId = chatRoomId)

        assertEquals(expectedMessages, result)
        verify(chatRoomService).getChatRoomById(chatRoomId, userId)
        verify(chatRoomService).getChatMessages(chatRoomId, null)
    }

    @Test
    fun `getMessagesForChatRoom throws ForbiddenException when requester is not participant`() {
        `when`(chatRoomService.getChatRoomById(chatRoomId, userId)).thenReturn(null)

        assertThrows<ForbiddenException> {
            controller.getMessagesForChatRoom(chatRoomId = chatRoomId)
        }

        verify(chatRoomService).getChatRoomById(chatRoomId, userId)
        verifyNoMoreInteractions(chatRoomService)
    }
}
