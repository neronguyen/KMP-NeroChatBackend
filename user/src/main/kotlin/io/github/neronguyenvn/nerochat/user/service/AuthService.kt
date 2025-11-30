package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.EmailNotVerifiedException
import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserAlreadyExistsException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.exception.WrongPasswordException
import io.github.neronguyenvn.nerochat.user.domain.model.AuthenticatedUser
import io.github.neronguyenvn.nerochat.user.domain.model.User
import io.github.neronguyenvn.nerochat.user.infra.database.model.RefreshTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.user.infra.database.repository.RefreshTokenRepository
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import kotlin.io.encoding.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional
    fun register(
        email: String,
        password: String,
    ): User {
        val existing = userRepository.findByEmail(email)

        if (existing != null) {
            throw UserAlreadyExistsException()
        }

        val saved = userRepository.saveAndFlush(
            UserEntity(
                email = email,
                hashedPassword = passwordEncoder.encode(password)!!,
            )
        )

        emailVerificationService.createVerificationToken(email)

        return saved.asExternalModel()
    }

    fun login(
        email: String,
        password: String,
    ): AuthenticatedUser {
        val existing = userRepository.findByEmail(email) ?: throw UserNotFoundException()

        val matches = passwordEncoder.matches(
            password,
            existing.hashedPassword
        )

        if (!matches) throw WrongPasswordException()
        if (!existing.isEmailVerified) throw EmailNotVerifiedException()

        val userId = existing.id ?: error("User ID cannot be null")
        val accessToken = jwtService.generateAccessToken(userId)
        val refreshToken = jwtService.generateRefreshToken(userId)

        saveRefreshToken(userId, refreshToken)

        return AuthenticatedUser(
            user = existing.asExternalModel(),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun refreshToken(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashedToken = hashToken(refreshToken)

        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        refreshTokenRepository.findByUserIdAndHashedToken(userId, hashedToken)
            ?: throw InvalidTokenException("Invalid refresh token")

        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashedToken)

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        saveRefreshToken(userId, newRefreshToken)

        return AuthenticatedUser(
            user = user.asExternalModel(),
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashToken = hashToken(refreshToken)
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashToken)
    }

    private fun saveRefreshToken(userId: UUID, refreshToken: String) {
        val hashedToken = hashToken(refreshToken)
        val expiryMillis = jwtService.refreshTokenValidityMs
        val expiredAt = Instant.now().plusMillis(expiryMillis)

        val entity = RefreshTokenEntity(
            userId = userId,
            hashedToken = hashedToken,
            expiredAt = expiredAt
        )

        refreshTokenRepository.save(entity)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(token.toByteArray())
        return Base64.encode(hashedBytes)
    }
}
