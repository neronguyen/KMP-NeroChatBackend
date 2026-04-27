package io.github.neronguyenvn.nerochat.user.domain.exception

class InvalidCredentialsException(override val message: String? = null) : RuntimeException(
    message ?: "Email or password is incorrect"
)