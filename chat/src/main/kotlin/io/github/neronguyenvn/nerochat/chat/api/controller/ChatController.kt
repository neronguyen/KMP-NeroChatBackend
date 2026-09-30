package io.github.neronguyenvn.nerochat.chat.api.controller

import io.github.neronguyenvn.nerochat.chat.api.model.ChatDto
import io.github.neronguyenvn.nerochat.chat.api.model.CreateChatRequest
import io.github.neronguyenvn.nerochat.chat.api.model.asDto
import io.github.neronguyenvn.nerochat.chat.service.ChatService
import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    @PostMapping
    fun createChat(
        @AuthenticationPrincipal requesterId: UserId,
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        val otherUserIds = body.otherUserIds.map { UserId(it) }.toSet()
        return chatService.createChat(
            creatorId = requesterId,
            otherUserIds = otherUserIds,
            messageContent = body.messageContent
        ).asDto()
    }
}
