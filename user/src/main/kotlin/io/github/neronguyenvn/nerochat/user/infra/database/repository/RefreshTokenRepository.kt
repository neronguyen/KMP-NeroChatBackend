package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.user.infra.database.model.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {

    fun findByUserIdAndHashedToken(userId: UUID, hashedToken: String): RefreshTokenEntity?

    fun deleteByUserIdAndHashedToken(userId: UUID, hashedToken: String)

    fun deleteByUserId(userId: UUID)
}

fun RefreshTokenRepository.findByUserIdAndHashedToken(
    userId: UserId,
    hashedToken: String,
): RefreshTokenEntity? = findByUserIdAndHashedToken(UUID.fromString(userId.value), hashedToken)

fun RefreshTokenRepository.deleteByUserIdAndHashedToken(userId: UserId, hashedToken: String) =
    deleteByUserIdAndHashedToken(UUID.fromString(userId.value), hashedToken)

fun RefreshTokenRepository.deleteByUserId(userId: UserId) =
    deleteByUserId(UUID.fromString(userId.value))
