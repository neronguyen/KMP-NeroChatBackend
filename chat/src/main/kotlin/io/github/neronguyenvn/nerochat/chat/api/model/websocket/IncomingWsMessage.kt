package io.github.neronguyenvn.nerochat.chat.api.model.websocket

import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.ChatMessageId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class IncomingWsMessage {

    @Serializable
    @SerialName(NEW_MESSAGE)
    data class NewMessage(
        val chatId: ChatId,
        val content: String,
        val messageId: ChatMessageId? = null
    ) : IncomingWsMessage()

    private companion object {
        const val NEW_MESSAGE = "new_message"
    }
}
