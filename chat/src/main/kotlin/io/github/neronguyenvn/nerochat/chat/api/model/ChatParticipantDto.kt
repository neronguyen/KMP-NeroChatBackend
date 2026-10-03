package io.github.neronguyenvn.nerochat.chat.api.model

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatParticipant
import kotlinx.serialization.Serializable

@Serializable
data class ChatParticipantDto(
    val userId: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String?
)

fun ChatParticipant.asDto(): ChatParticipantDto = ChatParticipantDto(
    userId = userId.value,
    email = email,
    displayName = displayName,
    profilePictureUrl = profilePictureUrl
)
