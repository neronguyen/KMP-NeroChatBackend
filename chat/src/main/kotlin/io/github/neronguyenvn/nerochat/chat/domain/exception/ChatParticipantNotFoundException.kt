package io.github.neronguyenvn.nerochat.chat.domain.exception

import io.github.neronguyenvn.nerochat.domain.type.UserId

class ChatParticipantNotFoundException(
    override val message: String
) : RuntimeException(message) {

    companion object {
        operator fun invoke(id: UserId): ChatParticipantNotFoundException =
            ChatParticipantNotFoundException("The chat participant with the ID $id was not found.")

        fun byEmail(email: String): ChatParticipantNotFoundException =
            ChatParticipantNotFoundException("The chat participant with the email $email was not found.")
    }
}
