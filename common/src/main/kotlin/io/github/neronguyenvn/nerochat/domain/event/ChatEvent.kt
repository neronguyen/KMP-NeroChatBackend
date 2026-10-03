package io.github.neronguyenvn.nerochat.domain.event

import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.UserId

sealed class ChatEvent : Event() {

    override val exchangeName: String = EXCHANGE_NAME

    data class NewMessage(
        val chatId: ChatId,
        val senderId: UserId,
        val message: String,
        override val key: String = CHAT_NEW_MESSAGE
    ) : ChatEvent()

    companion object {
        const val EXCHANGE_NAME = "chat.events"

        const val CHAT_NEW_MESSAGE = "chat.new_message"
    }
}
