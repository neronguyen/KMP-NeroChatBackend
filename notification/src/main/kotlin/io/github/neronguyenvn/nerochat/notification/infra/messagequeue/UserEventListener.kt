package io.github.neronguyenvn.nerochat.notification.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.UserEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class UserEventListener {

    @RabbitListener(queues = [UserEvent.QUEUE_NAME])
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> println("User created")
            is UserEvent.RequestResendVerification -> println("User requested resend verification")
            is UserEvent.RequestResetPassword -> println("User requested reset password")
            is UserEvent.Verified -> Unit
        }
    }
}
