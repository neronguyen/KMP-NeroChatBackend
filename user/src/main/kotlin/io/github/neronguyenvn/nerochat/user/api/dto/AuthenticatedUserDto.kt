package io.github.neronguyenvn.nerochat.user.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticatedUserDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)
