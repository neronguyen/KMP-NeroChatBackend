package io.github.neronguyenvn.nerochat.user.domain.exception

class EmailNotVerifiedException(
    override val message: String = "Email is not verified",
) : RuntimeException(message)