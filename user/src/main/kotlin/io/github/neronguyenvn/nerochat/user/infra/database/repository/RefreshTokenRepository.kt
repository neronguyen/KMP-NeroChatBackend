package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.user.infra.database.model.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {

    /**
     * Deletes stored refresh tokens matching both the user and token hash.
     * Return deleted count.
     */
    @Modifying
    @Query("""
        DELETE FROM RefreshTokenEntity rt WHERE rt.userId = :userId AND rt.hashedToken = :hashedToken
    """)
    fun deleteByUserIdAndHashedToken(userId: UUID, hashedToken: String): Long

    /** Deletes all stored refresh tokens for the user. */
    fun deleteByUserId(userId: UUID)
}

/**
 * Deletes stored refresh tokens matching both the user and token hash.
 * Return deleted count.
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
