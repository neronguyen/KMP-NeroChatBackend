package io.github.neronguyenvn.nerochat.user.domain.exception

class UserNotFoundException : RuntimeException(
    "A user with this email does not exist"
)
