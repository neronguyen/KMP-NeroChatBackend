package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.model.EmailVerificationToken
import io.github.neronguyenvn.nerochat.user.infra.database.model.EmailVerificationTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.user.infra.database.repository.EmailVerificationTokenRepository
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

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
}
