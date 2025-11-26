package io.github.neronguyenvn.nerochat.user.domain.model

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val isEmailVerified: Boolean,
)
