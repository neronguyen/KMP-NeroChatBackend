package io.github.neronguyenvn.nerochat.domain.event.user

import io.github.neronguyenvn.nerochat.domain.event.ChatEvent
import io.github.neronguyenvn.nerochat.domain.type.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
sealed class UserEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val exchange: String = EXCHANGE,
    override val occurredAt: Instant = Clock.System.now(),
) : ChatEvent() {

    @Serializable
    @SerialName(CREATED_KEY)
    data class Created(
        val userId: UserId,
        val email: String,
        val verificationToken: String,
        override val key: String = CREATED_KEY,
    ) : UserEvent()

    @Serializable
    @SerialName(VERIFIED_KEY)
    data class Verified(
        val userId: UserId,
        val email: String,
        override val key: String = VERIFIED_KEY,
    ) : UserEvent()

    @Serializable
    @SerialName(REQUEST_RESEND_VERIFICATION_KEY)
    data class RequestResendVerification(
        val userId: UserId,
        val email: String,
        val verificationToken: String,
        override val key: String = REQUEST_RESEND_VERIFICATION_KEY,
    ) : UserEvent()

    @Serializable
    @SerialName(REQUEST_RESET_PASSWORD_KEY)
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
