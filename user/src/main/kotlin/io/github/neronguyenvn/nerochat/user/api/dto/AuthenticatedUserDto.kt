package io.github.neronguyenvn.nerochat.user.api.dto

import io.github.neronguyenvn.nerochat.user.domain.model.AuthenticatedUser
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticatedUserDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

fun AuthenticatedUser.asDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.asDto(),
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}
