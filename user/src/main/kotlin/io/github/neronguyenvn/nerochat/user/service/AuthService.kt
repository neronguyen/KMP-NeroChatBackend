package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.PasswordMismatchException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserAlreadyExistsException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.model.AuthenticatedUser
import io.github.neronguyenvn.nerochat.user.domain.model.User
import io.github.neronguyenvn.nerochat.user.infra.database.model.RefreshTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.user.infra.database.repository.RefreshTokenRepository
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
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
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun register(
        email: String,
        password: String,
    ): User {
        val existing = userRepository.findByEmail(email)

        if (existing != null) {
            throw UserAlreadyExistsException()
        }

        val saved = userRepository.save(
            UserEntity(
                email = email,
                hashedPassword = passwordEncoder.encode(password)!!,
            )
        )

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

        if (!matches) throw PasswordMismatchException()

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

    private fun saveRefreshToken(userId: UUID, refreshToken: String) {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(refreshToken.toByteArray())
        val hashedToken = Base64.encode(hashedBytes)

        val expiryMillis = jwtService.refreshTokenValidityMs
        val expiredAt = Instant.now().plusMillis(expiryMillis)

        val entity = RefreshTokenEntity(
            userId = userId,
            hashedToken = hashedToken,
            expiredAt = expiredAt
        )

        refreshTokenRepository.save(entity)
    }
}
