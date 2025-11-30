package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.model.AuthToken
import io.github.neronguyenvn.nerochat.user.domain.model.AuthTokenType
import io.github.neronguyenvn.nerochat.user.infra.database.model.AuthTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asEmailVerificationToken
import io.github.neronguyenvn.nerochat.user.infra.database.repository.AuthTokenRepository
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
import io.github.neronguyenvn.nerochat.user.infra.database.security.SecureTokenGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val authTokenRepository: AuthTokenRepository,
    private val userRepository: UserRepository,
    @param:Value($$"${email.verification.expiry-hours}") private val expiryHours: Long
) {
    @Transactional
    fun createVerificationToken(email: String): AuthToken.EmailVerification {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        authTokenRepository.invalidateEmailVerificationTokens(user)

        val expiryDate = Instant.now().plus(expiryHours, ChronoUnit.HOURS)

        val token = AuthTokenEntity(
            token = SecureTokenGenerator.generate(),
            expiredAt = expiryDate,
            tokenType = AuthTokenType.EmailVerification,
            user = user
        )

        return authTokenRepository.save(token).asEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val existing = authTokenRepository.findByIdOrNull(token)
            ?: throw InvalidTokenException("Email verification token is invalid")

        if (existing.isUsed) {
            throw InvalidTokenException("Email verification token is already used")
        }

        if (existing.isExpired) {
            throw InvalidTokenException("Email verification token is expired")
        }

        val now = Instant.now()
        val usedToken = existing.copy(usedAt = now)
        authTokenRepository.save(usedToken)
        userRepository.save(existing.user.copy(isEmailVerified = true))
    }
}
