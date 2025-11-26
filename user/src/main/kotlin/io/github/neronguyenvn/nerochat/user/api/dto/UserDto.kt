package io.github.neronguyenvn.nerochat.user.api.dto

import java.util.UUID

data class UserDto(
    val id: UUID,
    val email: String,
    val isEmailVerified: Boolean,
)
