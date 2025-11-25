package io.github.neronguyenvn.nerochat.user.domain.exception

class UserAlreadyExistsException : RuntimeException(
    "A user with this username or email already exists."
)
