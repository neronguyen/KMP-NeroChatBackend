package io.github.neronguyenvn.nerochat.user.domain.exception

class SamePasswordException(
    override val message: String = "New password cannot be the same as the old password"
) : RuntimeException(message)