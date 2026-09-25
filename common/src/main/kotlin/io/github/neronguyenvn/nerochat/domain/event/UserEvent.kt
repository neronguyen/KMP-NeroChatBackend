package io.github.neronguyenvn.nerochat.domain.event

import io.github.neronguyenvn.nerochat.domain.type.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed class UserEvent(
    override val exchangeName: String = EXCHANGE_NAME,
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
        val expiresInMinutes: Duration,
        override val key: String = REQUEST_RESET_PASSWORD_KEY,
    ) : UserEvent()

    companion object {
        const val EXCHANGE_NAME = "user.events"

        const val CREATED_KEY = "user.created"
        const val VERIFIED_KEY = "user.verified"
        const val REQUEST_RESEND_VERIFICATION_KEY = "user.request_resend_verification"
        const val REQUEST_RESET_PASSWORD_KEY = "user.request_reset_password"
    }
}
