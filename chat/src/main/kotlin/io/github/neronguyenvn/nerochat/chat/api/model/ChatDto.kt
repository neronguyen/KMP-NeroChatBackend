package io.github.neronguyenvn.nerochat.chat.api.model

import io.github.neronguyenvn.nerochat.chat.domain.model.Chat
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class ChatDto(
    val id: String,
    val creator: ChatParticipantDto,
    val participants: List<ChatParticipantDto>,
    val lastMessage: ChatMessageDto,
    val lastActivityAt: Instant,
)

@OptIn(ExperimentalTime::class)
fun Chat.asDto(): ChatDto = ChatDto(
    id = id.value,
    creator = creator.asDto(),
    participants = participants.map { it.asDto() },
    lastMessage = lastMessage.asDto(),
    lastActivityAt = lastActivityAt,
)
