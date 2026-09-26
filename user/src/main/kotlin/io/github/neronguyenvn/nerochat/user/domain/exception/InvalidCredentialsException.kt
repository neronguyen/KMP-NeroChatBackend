package io.github.neronguyenvn.nerochat.user.domain.exception

class InvalidCredentialsException(
    override val message: String = "Email or password is incorrect"
) : RuntimeException(message)