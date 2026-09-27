package io.github.neronguyenvn.nerochat.notification.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.UserEvent
import io.github.neronguyenvn.nerochat.notification.service.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class UserEventListener(
    private val emailService: EmailService
) {

    /**
     * Dispatches registration, verification-resend, and password-reset events to the email service.
     * Verified events require no email and are ignored. Email-service errors propagate to the listener container.
     */
    @RabbitListener(queues = [UserEvent.QUEUE_NAME])
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> emailService.sendVerificationEmail(
                userId = event.userId,
                email = event.email,
                displayName = event.displayName,
                token = event.verificationToken
            )

            is UserEvent.RequestResendVerification -> emailService.sendVerificationEmail(
                userId = event.userId,
                email = event.email,
                displayName = event.displayName,
                token = event.verificationToken
            )

            is UserEvent.RequestResetPassword -> emailService.sendPasswordResetEmail(
                userId = event.userId,
                email = event.email,
                displayName = event.displayName,
                token = event.passwordResetToken,
                expiresIn = event.expiresIn
            )

            is UserEvent.Verified -> Unit
        }
    }
}
