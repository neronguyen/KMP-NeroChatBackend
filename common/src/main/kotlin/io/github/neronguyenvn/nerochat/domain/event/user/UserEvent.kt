package io.github.neronguyenvn.nerochat.domain.event.user

import io.github.neronguyenvn.nerochat.domain.event.ChatEvent
import io.github.neronguyenvn.nerochat.domain.type.UserId
import java.time.Instant
import java.util.*

sealed class UserEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val exchange: String = EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
) : ChatEvent() {

    data class Created(
        val userId: UserId,
        val email: String,
        val verificationToken: String,
        override val key: String = CREATED_KEY,
    ) : UserEvent()

    data class Verified(
        val userId: UserId,
        val email: String,
        override val key: String = VERIFIED_KEY,
    ) : UserEvent()

    data class RequestResendVerification(
        val userId: UserId,
        val email: String,
        val verificationToken: String,
        override val key: String = REQUEST_RESEND_VERIFICATION_KEY,
    ) : UserEvent()

    data class RequestResetPassword(
        val userId: UserId,
        val email: String,
        val verificationToken: String,
        val expiresInMinutes: Long,
        override val key: String = REQUEST_RESET_PASSWORD_KEY,
    ) : UserEvent()

    companion object {
        const val EXCHANGE = "user.events"

        const val CREATED_KEY = "user.created"
        const val VERIFIED_KEY = "user.verified"
        const val REQUEST_RESEND_VERIFICATION_KEY = "user.request_resend_verification"
        const val REQUEST_RESET_PASSWORD_KEY = "user.request_reset_password"
    }
}
