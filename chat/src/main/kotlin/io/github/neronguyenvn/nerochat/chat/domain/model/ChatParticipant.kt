package io.github.neronguyenvn.nerochat.chat.domain.model

import io.github.neronguyenvn.nerochat.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val displayName: String,
    val email: String,
    val profilePictureUrl: String?
)
