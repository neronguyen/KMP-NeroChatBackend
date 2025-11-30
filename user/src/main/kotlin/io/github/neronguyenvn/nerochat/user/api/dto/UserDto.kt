package io.github.neronguyenvn.nerochat.user.api.dto

import io.github.neronguyenvn.nerochat.user.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val isEmailVerified: Boolean,
)

fun User.asDto(): UserDto {
    return UserDto(
        id = id.toString(),
        email = email,
        isEmailVerified = isEmailVerified
    )
}
