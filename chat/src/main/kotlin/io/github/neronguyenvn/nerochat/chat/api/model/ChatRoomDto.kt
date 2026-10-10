package io.github.neronguyenvn.nerochat.chat.api.model

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatRoom
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class ChatRoomDto(
    val id: String,
    val creator: ChatParticipantDto,
    val participants: List<ChatParticipantDto>,
    val lastMessage: ChatMessageDto,
    val lastActivityAt: Instant,
)

@OptIn(ExperimentalTime::class)
fun ChatRoom.asDto(): ChatRoomDto = ChatRoomDto(
    id = id.value,
    creator = creator.asDto(),
    participants = participants.map { it.asDto() },
    lastMessage = lastMessage.asDto(),
    lastActivityAt = lastActivityAt,
)
