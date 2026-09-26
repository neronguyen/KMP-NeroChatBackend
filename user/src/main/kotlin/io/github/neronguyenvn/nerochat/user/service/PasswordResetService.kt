package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.domain.event.UserEvent
import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.infra.messagequeue.EventPublisher
import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.github.neronguyenvn.nerochat.user.domain.exception.SamePasswordException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.exception.WrongPasswordException
import io.github.neronguyenvn.nerochat.user.domain.model.AuthToken
import io.github.neronguyenvn.nerochat.user.domain.model.AuthTokenType
import io.github.neronguyenvn.nerochat.user.infra.database.model.AuthTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asPasswordResetToken
import io.github.neronguyenvn.nerochat.user.infra.database.model.userId
import io.github.neronguyenvn.nerochat.user.infra.database.repository.*
import io.github.neronguyenvn.nerochat.user.infra.security.SecureTokenGenerator
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class PasswordResetService(
    private val authTokenRepository: AuthTokenRepository,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: EventPublisher,
    @param:Value($$"${email.password-reset.expiry-minutes}") private val expiryMinutes: Long
) {
    @Transactional
    fun requestPasswordReset(email: String): AuthToken.PasswordReset {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        authTokenRepository.invalidatePasswordResetTokens(user)

        val expiryDate = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        val token = AuthTokenEntity(
            token = SecureTokenGenerator.generate(),
            expiredAt = expiryDate,
            tokenType = AuthTokenType.PasswordReset,
            user = user
        )

        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.userId,
                email = user.email,
                passwordResetToken = token.token,
                expiresIn = expiryMinutes.toDuration(DurationUnit.MINUTES)
            )
        )

        return authTokenRepository.save(token).asPasswordResetToken()
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val existingToken = authTokenRepository.findByIdOrNull(token)
            ?: throw InvalidTokenException("Password reset token is invalid")

        if (existingToken.isUsed()) {
            throw InvalidTokenException("Password reset token is already used")
        }

        if (existingToken.isExpired()) {
            throw InvalidTokenException("Password reset token is expired")
        }

        val user = existingToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.userId)

        val newHashedPassword = passwordEncoder.encode(newPassword)!!
        user.hashedPassword = newHashedPassword
        userRepository.save(user)

        existingToken.usedAt = Instant.now()
        authTokenRepository.save(existingToken)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByUserId(userId)
            ?: error("User cannot be null")

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw WrongPasswordException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.userId)

        val newHashedPassword = passwordEncoder.encode(newPassword)!!
        user.hashedPassword = newHashedPassword
        userRepository.save(user)
    }
}
