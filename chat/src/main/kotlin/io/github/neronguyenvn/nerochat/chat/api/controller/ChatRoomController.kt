package io.github.neronguyenvn.nerochat.chat.api.controller

import io.github.neronguyenvn.nerochat.api.util.requesterId
import io.github.neronguyenvn.nerochat.chat.api.model.*
import io.github.neronguyenvn.nerochat.chat.service.ChatRoomService
import io.github.neronguyenvn.nerochat.domain.exception.ForbiddenException
import io.github.neronguyenvn.nerochat.domain.type.ChatRoomId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping(value = ["/api/chat-rooms", "/api/chat"])
class ChatRoomController(private val chatRoomService: ChatRoomService) {

    @GetMapping("/{chatRoomId}/messages")
    fun getMessagesForChatRoom(
        @PathVariable("chatRoomId") chatRoomId: ChatRoomId,
        @RequestParam("before", required = false) before: Instant? = null,
    ): List<ChatMessageDto> {
        chatRoomService.getChatRoomById(chatRoomId, requesterId)
            ?: throw ForbiddenException()

        return chatRoomService.getChatMessages(
            chatRoomId = chatRoomId,
            before = before,
        )
    }

    @GetMapping("/{chatRoomId}")
    fun getChatRoom(
        @PathVariable("chatRoomId") chatRoomId: ChatRoomId,
    ): ChatRoomDto {
        return chatRoomService
            .getChatRoomById(chatRoomId = chatRoomId, requesterId = requesterId)
            ?.asDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getChatRoomsForUser(): List<ChatRoomDto> {
        return chatRoomService
            .findChatRoomsByUser(userId = requesterId)
            .map { it.asDto() }
    }

    @PostMapping("/direct")
    fun createDirectChatRoom(
        @Valid @RequestBody body: CreateDirectChatRoomRequest,
    ): ChatRoomDto {
        return chatRoomService.createDirectChatRoom(
            creatorId = requesterId,
            targetUserId = UserId(body.targetUserId),
            message = body.message
        ).asDto()
    }

    @PostMapping("/group")
    fun createGroupChatRoom(
        @Valid @RequestBody body: CreateGroupChatRoomRequest,
    ): ChatRoomDto {
        return chatRoomService.createGroupChatRoom(
            creatorId = requesterId,
            name = body.name,
            participantIds = body.participantIds.map { UserId(it) }
        ).asDto()
    }

    @PostMapping("/{chatRoomId}/add")
    fun addChatRoomParticipants(
        @PathVariable("chatRoomId") chatRoomId: ChatRoomId,
        @Valid @RequestBody body: AddChatParticipantRequest,
    ): ChatRoomDto {
        return chatRoomService.addParticipantsToChatRoom(
            requesterId = requesterId,
            chatRoomId = chatRoomId,
            userIds = body.userIds.toSet()
        ).asDto()
    }

    @DeleteMapping("/{chatRoomId}/leave")
    fun leaveChatRoom(
        @PathVariable("chatRoomId") chatRoomId: ChatRoomId,
    ) {
        chatRoomService.removeParticipantFromChatRoom(
            chatRoomId = chatRoomId,
            userId = requesterId
        )
    }
}
