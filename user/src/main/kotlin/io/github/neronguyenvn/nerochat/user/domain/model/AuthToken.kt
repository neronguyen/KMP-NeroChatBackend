package io.github.neronguyenvn.nerochat.user.domain.model

sealed class AuthToken(
    open val token: String,
    open val user: User,
) {

    data class EmailVerificationToken(
        override val token: String,
        override val user: User,
    ) : AuthToken(token, user)
}

enum class AuthTokenType {
    EmailVerification;
}
