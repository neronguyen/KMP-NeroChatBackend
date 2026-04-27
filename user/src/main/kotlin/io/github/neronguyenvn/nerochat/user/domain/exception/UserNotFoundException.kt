package io.github.neronguyenvn.nerochat.user.domain.exception

class UserNotFoundException(
    override val message: String = "A user with this email does not exist"
) : RuntimeException(message)