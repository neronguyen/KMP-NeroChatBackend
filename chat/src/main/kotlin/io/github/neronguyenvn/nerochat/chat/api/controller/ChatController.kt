package io.github.neronguyenvn.nerochat.chat.api.controller

import io.github.neronguyenvn.nerochat.api.util.requesterId
import io.github.neronguyenvn.nerochat.chat.api.model.*
import io.github.neronguyenvn.nerochat.chat.service.ChatService
import io.github.neronguyenvn.nerochat.domain.exception.ForbiddenException
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    @GetMapping("/{chatId}/messages")
    fun getMessagesForChat(
        @PathVariable("chatId") chatId: ChatId,
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<ChatMessageDto> {
        chatService.getChatById(chatId, requesterId)
            ?: throw ForbiddenException()

        return chatService.getChatMessages(
            chatId = chatId,
            before = before,
            pageSize = pageSize
        )
    }

    @GetMapping("/{chatId}")
    fun getChat(
        @PathVariable("chatId") chatId: ChatId,
    ): ChatDto {
        return chatService
            .getChatById(chatId = chatId, requesterId = requesterId)
            ?.asDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getChatsForUser(): List<ChatDto> {
        return chatService
            .findChatsByUser(userId = requesterId)
            .map { it.asDto() }
    }

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest,
    ): ChatDto {
        val otherUserIds = body.otherUserIds.map { UserId(it) }.toSet()
        return chatService.createChat(
            creatorId = requesterId,
            otherUserIds = otherUserIds,
            messageContent = body.messageContent
        ).asDto()
    }

    @PostMapping("/{chatId}/add")
    fun addChatParticipants(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody body: AddChatParticipantRequest,
    ): ChatDto {
        return chatService.addParticipantsToChat(
            requesterId = requesterId,
            chatId = chatId,
            userIds = body.userIds.toSet()
        ).asDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun leaveChat(
        @PathVariable chatId: ChatId,
    ) {
        chatService.removeParticipantFromChat(
            chatId = chatId,
            userId = requesterId
        )
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }
}
