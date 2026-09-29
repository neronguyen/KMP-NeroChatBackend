package io.github.neronguyenvn.nerochat.chat.domain.exception

import io.github.neronguyenvn.nerochat.domain.type.UserId

class ChatParticipantNotFoundException(id: UserId) : RuntimeException(
    "The chat participant with the ID $id was not found."
)
