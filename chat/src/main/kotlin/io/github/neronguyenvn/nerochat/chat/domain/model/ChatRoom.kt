package io.github.neronguyenvn.nerochat.chat.domain.model

import io.github.neronguyenvn.nerochat.domain.type.ChatRoomId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class ChatRoom(
    val id: ChatRoomId,
    val creator: ChatParticipant,
    val participants: Set<ChatParticipant>,
    val lastMessage: ChatMessage,
    val lastActivityAt: Instant,
    val createdAt: Instant
)
