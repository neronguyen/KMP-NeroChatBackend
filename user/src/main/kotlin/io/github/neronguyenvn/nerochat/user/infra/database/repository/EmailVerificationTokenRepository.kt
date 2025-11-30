package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.user.infra.database.model.EmailVerificationTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationTokenEntity, UUID> {

    @Modifying
    @Query(
        """
        UPDATE EmailVerificationTokenEntity entity
        SET entity.usedAt = CURRENT_TIMESTAMP
        WHERE entity.user = :user
    """
    )
    fun invalidateActiveTokens(user: UserEntity)

    fun deleteByExpiredAtBefore(now: Instant)
}
