package io.github.neronguyenvn.nerochat.domain.exception

class InvalidTokenException(
    override val message: String = "Invalid token"
) : RuntimeException(message)
