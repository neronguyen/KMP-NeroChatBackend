package io.github.neronguyenvn.nerochat.chat.domain.exception

import io.github.neronguyenvn.nerochat.domain.type.ChatMessageId

class MessageNotFoundException(id: ChatMessageId) : RuntimeException(
    "Message with ID $id not found"
)
