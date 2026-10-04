package io.github.neronguyenvn.nerochat.domain.exception

class UnauthorizedException(
    override val message: String = "Missing auth details"
) : RuntimeException(message)
