package io.github.neronguyenvn.nerochat.user.domain.exception

class UserAlreadyExistsException(
    override val message: String = "A user with this email already exists",
) : RuntimeException(message)