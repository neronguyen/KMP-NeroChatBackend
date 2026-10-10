package io.github.neronguyenvn.nerochat.chat.domain.exception

class InvalidChatRoomSizeException(
    message: String = "There must be at least 2 unique participants to create a chat room."
) : RuntimeException(message)
