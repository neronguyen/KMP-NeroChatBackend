package io.github.neronguyenvn.nerochat.user.domain.exception

class InvalidTokenException(override val message: String? = null) : RuntimeException(
    message ?: "Invalid token"
)
