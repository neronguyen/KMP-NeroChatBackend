package io.github.neronguyenvn.nerochat.chat.api.controller

import io.github.neronguyenvn.nerochat.api.util.requesterId
import io.github.neronguyenvn.nerochat.chat.api.model.ChatParticipantDto
import io.github.neronguyenvn.nerochat.chat.api.model.asDto
import io.github.neronguyenvn.nerochat.chat.service.ChatParticipantService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/chat/participants")
class ChatParticipantController(private val chatParticipantService: ChatParticipantService) {

    @GetMapping
    fun getChatParticipantByEmail(
        @RequestParam(required = false) email: String?,
    ): ChatParticipantDto {
        val participant = if (email.isNullOrBlank()) {
            chatParticipantService.findChatParticipantById(userId = requesterId)
        } else {
            chatParticipantService.findChatParticipantByEmail(email = email)
        }

        return participant?.asDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}
