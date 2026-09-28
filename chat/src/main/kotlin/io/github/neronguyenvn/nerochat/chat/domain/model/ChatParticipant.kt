package io.github.neronguyenvn.nerochat.chat.domain.model

import io.github.neronguyenvn.nerochat.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
