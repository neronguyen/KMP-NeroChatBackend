package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.github.neronguyenvn.nerochat.user.domain.exception.SamePasswordException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.exception.WrongPasswordException
import io.github.neronguyenvn.nerochat.user.domain.model.AuthToken
import io.github.neronguyenvn.nerochat.user.domain.model.AuthTokenType
import io.github.neronguyenvn.nerochat.user.infra.database.model.AuthTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asPasswordResetToken
import io.github.neronguyenvn.nerochat.user.infra.database.repository.AuthTokenRepository
import io.github.neronguyenvn.nerochat.user.infra.database.repository.RefreshTokenRepository
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
import io.github.neronguyenvn.nerochat.user.infra.database.security.SecureTokenGenerator
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class PasswordResetService(
    private val authTokenRepository: AuthTokenRepository,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
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

        return authTokenRepository.save(token).asPasswordResetToken()
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val existingToken = authTokenRepository.findByIdOrNull(token)
            ?: throw InvalidTokenException("Password reset token is invalid")

        if (existingToken.isUsed) {
            throw InvalidTokenException("Password reset  token is already used")
        }

        if (existingToken.isExpired) {
            throw InvalidTokenException("Password reset token is expired")
        }

        val user = existingToken.user

        if (!passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newHashedPassword = passwordEncoder.encode(newPassword)!!
        userRepository.save(user.copy(hashedPassword = newHashedPassword))
    }

    @Transactional
    fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String
    ) {
        val userId = jwtService.getUserIdFromToken(accessToken)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw WrongPasswordException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newPasswordHash = passwordEncoder.encode(newPassword)!!
        userRepository.save(user.copy(hashedPassword = newPasswordHash))
    }
}
