package io.github.neronguyenvn.nerochat.user.domain.exception

class InvalidTokenException(override val message: String?) : RuntimeException(
    message ?: "Invalid token"
)
