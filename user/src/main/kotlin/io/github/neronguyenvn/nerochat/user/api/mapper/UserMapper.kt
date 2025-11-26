package io.github.neronguyenvn.nerochat.user.api.mapper

import io.github.neronguyenvn.nerochat.user.api.dto.AuthenticatedUserDto
import io.github.neronguyenvn.nerochat.user.api.dto.UserDto
import io.github.neronguyenvn.nerochat.user.domain.model.AuthenticatedUser
import io.github.neronguyenvn.nerochat.user.domain.model.User

fun AuthenticatedUser.asDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.asDto(),
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}

fun User.asDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        isEmailVerified = isEmailVerified
    )
}
