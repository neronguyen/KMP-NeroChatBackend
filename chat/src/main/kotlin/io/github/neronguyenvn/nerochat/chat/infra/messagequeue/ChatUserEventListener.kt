package io.github.neronguyenvn.nerochat.chat.infra.messagequeue

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatParticipant
import io.github.neronguyenvn.nerochat.chat.service.ChatParticipantService
import io.github.neronguyenvn.nerochat.domain.event.UserEvent
import io.github.neronguyenvn.nerochat.infra.messagequeue.MessageQueue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(private val chatParticipantService: ChatParticipantService) {

    @RabbitListener(queues = [MessageQueue.CHAT_SERVICE_FOR_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        if (event is UserEvent.Verified) {
            chatParticipantService.createChatParticipant(
                chatParticipant = ChatParticipant(
                    userId = event.userId,
                    email = event.email,
                    displayName = event.displayName,
                    profilePictureUrl = null
                )
            )
        }
    }
}
