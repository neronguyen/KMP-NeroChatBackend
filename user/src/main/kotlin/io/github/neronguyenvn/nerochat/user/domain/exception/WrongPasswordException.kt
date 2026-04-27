package io.github.neronguyenvn.nerochat.user.domain.exception

class WrongPasswordException(
    override val message: String = "Wrong password provided",
) : RuntimeException(message)