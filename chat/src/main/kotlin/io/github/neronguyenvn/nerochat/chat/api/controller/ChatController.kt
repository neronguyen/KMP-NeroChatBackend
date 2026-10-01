package io.github.neronguyenvn.nerochat.chat.api.controller

import io.github.neronguyenvn.nerochat.chat.api.model.AddChatParticipantRequest
import io.github.neronguyenvn.nerochat.chat.api.model.ChatDto
import io.github.neronguyenvn.nerochat.chat.api.model.CreateChatRequest
import io.github.neronguyenvn.nerochat.chat.api.model.asDto
import io.github.neronguyenvn.nerochat.chat.service.ChatService
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest,
        @AuthenticationPrincipal requesterId: UserId,
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
        @AuthenticationPrincipal requesterId: UserId,
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
        @AuthenticationPrincipal requesterId: UserId,
    ) {
        chatService.removeParticipantFromChat(
            chatId = chatId,
            userId = requesterId
        )
    }
}
