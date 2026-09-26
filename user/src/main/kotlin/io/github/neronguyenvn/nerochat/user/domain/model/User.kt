package io.github.neronguyenvn.nerochat.user.domain.model

import io.github.neronguyenvn.nerochat.domain.type.UserId

data class User(
    val id: UserId,
    val email: String,
    val displayName: String,
    val isEmailVerified: Boolean,
)
