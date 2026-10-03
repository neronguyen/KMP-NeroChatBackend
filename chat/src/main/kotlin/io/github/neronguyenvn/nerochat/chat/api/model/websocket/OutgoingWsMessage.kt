package io.github.neronguyenvn.nerochat.chat.api.model.websocket

import io.github.neronguyenvn.nerochat.chat.api.model.ChatMessageDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class OutgoingWsMessage {

    @Serializable
    @SerialName(NEW_MESSAGE)
    data class NewMessage(
        val message: ChatMessageDto
    ) : OutgoingWsMessage()

    @Serializable
    @SerialName(ERROR)
    data class Error(
        val code: String,
        val message: String
    ) : OutgoingWsMessage()

    private companion object {
        const val NEW_MESSAGE = "new_message"
        const val ERROR = "error"
    }
}
