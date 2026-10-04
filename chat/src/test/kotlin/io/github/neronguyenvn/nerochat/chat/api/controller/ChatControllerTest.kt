package io.github.neronguyenvn.nerochat.chat.api.controller

import io.github.neronguyenvn.nerochat.chat.api.model.ChatMessageDto
import io.github.neronguyenvn.nerochat.chat.domain.model.Chat
import io.github.neronguyenvn.nerochat.chat.service.ChatService
import io.github.neronguyenvn.nerochat.domain.exception.ForbiddenException
import io.github.neronguyenvn.nerochat.domain.type.ChatId
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
class ChatControllerTest {

    private lateinit var chatService: ChatService
    private lateinit var controller: ChatController

    private val userId = UserId(UUID.randomUUID())
    private val chatId = ChatId(UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        chatService = mock(ChatService::class.java)
        controller = ChatController(chatService)

        val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getMessagesForChat returns messages when requester is participant`() {
        val chat = mock(Chat::class.java)
        `when`(chatService.getChatById(chatId, userId)).thenReturn(chat)

        val expectedMessages = listOf(
            ChatMessageDto(
                id = UUID.randomUUID().toString(),
                chatId = chatId.value,
                senderId = userId.value,
                content = "Hello",
                createdAt = Clock.System.now()
            )
        )
        `when`(chatService.getChatMessages(chatId, null, 20)).thenReturn(expectedMessages)

        val result = controller.getMessagesForChat(chatId = chatId)

        assertEquals(expectedMessages, result)
        verify(chatService).getChatById(chatId, userId)
        verify(chatService).getChatMessages(chatId, null, 20)
    }

    @Test
    fun `getMessagesForChat throws ForbiddenException when requester is not participant`() {
        `when`(chatService.getChatById(chatId, userId)).thenReturn(null)

        assertThrows<ForbiddenException> {
            controller.getMessagesForChat(chatId = chatId)
        }

        verify(chatService).getChatById(chatId, userId)
        verifyNoMoreInteractions(chatService)
    }
}
