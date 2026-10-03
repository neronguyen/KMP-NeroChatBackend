package io.github.neronguyenvn.nerochat.chat.domain.event

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatParticipant
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.ChatMessageId

internal sealed class InternalChatEvent {

    data class ChatParticipantJoinedEvent(
        val chatId: ChatId,
        val newUsers: Set<ChatParticipant>
    ) : InternalChatEvent()

    data class ChatParticipantLeftEvent(
        val chatId: ChatId,
        val leftUser: ChatParticipant
    ) : InternalChatEvent()

    data class MessageDeletedEvent(
        val chatId: ChatId,
        val messageId: ChatMessageId,
    ) : InternalChatEvent()
}
