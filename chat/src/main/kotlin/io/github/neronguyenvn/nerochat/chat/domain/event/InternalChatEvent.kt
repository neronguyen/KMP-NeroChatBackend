package io.github.neronguyenvn.nerochat.chat.domain.event

import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.ChatMessageId
import io.github.neronguyenvn.nerochat.domain.type.UserId

internal sealed class InternalChatEvent {

    data class ChatParticipantJoinedEvent(
        val chatId: ChatId,
        val userIds: Set<UserId>
    ) : InternalChatEvent()

    data class ChatParticipantLeftEvent(
        val chatId: ChatId,
        val userId: UserId
    ) : InternalChatEvent()

    data class MessageDeletedEvent(
        val chatId: ChatId,
        val messageId: ChatMessageId,
    ) : InternalChatEvent()
}
