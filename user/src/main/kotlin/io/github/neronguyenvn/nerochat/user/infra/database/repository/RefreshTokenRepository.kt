package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.user.infra.database.model.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {

    /** Returns the matching stored refresh token, or null; expiration is not checked. */
    fun findByUserIdAndHashedToken(userId: UUID, hashedToken: String): RefreshTokenEntity?

    /** Deletes stored refresh tokens matching both the user and token hash. */
    fun deleteByUserIdAndHashedToken(userId: UUID, hashedToken: String)

    /** Deletes all stored refresh tokens for the user. */
    fun deleteByUserId(userId: UUID)
}

/**
 * Returns the matching stored refresh token, or null; expiration is not checked.
 *
 * @throws IllegalArgumentException if [userId] cannot be parsed as a UUID.
 */
fun RefreshTokenRepository.findByUserIdAndHashedToken(
    userId: UserId,
    hashedToken: String,
): RefreshTokenEntity? = findByUserIdAndHashedToken(UUID.fromString(userId.value), hashedToken)

/**
 * Deletes stored refresh tokens matching both the user and token hash.
 *
 * @throws IllegalArgumentException if [userId] cannot be parsed as a UUID.
 */
fun RefreshTokenRepository.deleteByUserIdAndHashedToken(userId: UserId, hashedToken: String) =
    deleteByUserIdAndHashedToken(UUID.fromString(userId.value), hashedToken)

/**
 * Deletes all stored refresh tokens for the user.
 *
 * @throws IllegalArgumentException if [userId] cannot be parsed as a UUID.
 */
fun RefreshTokenRepository.deleteByUserId(userId: UserId) =
    deleteByUserId(UUID.fromString(userId.value))
