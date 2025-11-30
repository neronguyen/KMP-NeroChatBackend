package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.model.EmailVerificationToken
import io.github.neronguyenvn.nerochat.user.infra.database.model.EmailVerificationTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.user.infra.database.repository.EmailVerificationTokenRepository
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value($$"${email.verification.expiry-hours}") private val expiryHours: Long
) {
    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        val existingTokens = emailVerificationTokenRepository.findByUserAndUsedAtIsNull(
            user = user
        )

        val now = Instant.now()
        val usedTokens = existingTokens.map { it.copy(usedAt = now) }
        emailVerificationTokenRepository.saveAll(usedTokens)

        val expiryDate = now.plus(expiryHours, ChronoUnit.HOURS)

        val token = EmailVerificationTokenEntity(
            expiredAt = expiryDate,
            user = user
        )

        return emailVerificationTokenRepository.save(token).asExternalModel()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val existing = emailVerificationTokenRepository.findByIdOrNull(
            UUID.fromString(token)
        ) ?: throw InvalidTokenException("Email verification token is invalid")

        if (existing.isUsed) {
            throw InvalidTokenException("Email verification token is already used")
        }

        if (existing.isExpired) {
            throw InvalidTokenException("Email verification token is expired")
        }

        val now = Instant.now()
        val usedToken = existing.copy(usedAt = now)
        emailVerificationTokenRepository.save(usedToken)
        userRepository.save(existing.user.copy(isEmailVerified = true))
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpExpiredTokens() {
        val now = Instant.now()
        emailVerificationTokenRepository.deleteByExpiredAtBefore(now)
    }
}
