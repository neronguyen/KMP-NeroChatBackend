package io.github.neronguyenvn.nerochat.chat.api.model

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatRoom
import io.github.neronguyenvn.nerochat.chat.domain.model.ChatRoomType
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class ChatRoomDto(
    val id: String,
    val type: ChatRoomType,
    val name: String? = null,
    val creator: ChatParticipantDto,
    val participants: List<ChatParticipantDto>,
    val lastMessage: ChatMessageDto? = null,
    val lastActivityAt: Instant,
)

@OptIn(ExperimentalTime::class)
fun ChatRoom.asDto(): ChatRoomDto = ChatRoomDto(
    id = id.value,
    type = type,
    name = name,
    creator = creator.asDto(),
    participants = participants.map { it.asDto() },
    lastMessage = lastMessage?.asDto(),
    lastActivityAt = lastActivityAt,
)
