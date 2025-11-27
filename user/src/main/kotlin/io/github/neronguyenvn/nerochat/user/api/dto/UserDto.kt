package io.github.neronguyenvn.nerochat.user.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val isEmailVerified: Boolean,
)
