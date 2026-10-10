package io.github.neronguyenvn.nerochat.chat.domain.event

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatParticipant
import io.github.neronguyenvn.nerochat.domain.type.ChatMessageId
import io.github.neronguyenvn.nerochat.domain.type.ChatRoomId

internal sealed class InternalChatEvent {

    data class ChatParticipantJoinedEvent(
        val chatRoomId: ChatRoomId,
        val newUsers: Set<ChatParticipant>
    ) : InternalChatEvent()

    data class ChatParticipantLeftEvent(
        val chatRoomId: ChatRoomId,
        val leftUser: ChatParticipant
    ) : InternalChatEvent()

    data class MessageDeletedEvent(
        val chatRoomId: ChatRoomId,
        val messageId: ChatMessageId,
    ) : InternalChatEvent()
}
