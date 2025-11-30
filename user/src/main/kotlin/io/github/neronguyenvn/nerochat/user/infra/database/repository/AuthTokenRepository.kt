package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.user.domain.model.AuthTokenType
import io.github.neronguyenvn.nerochat.user.infra.database.model.AuthTokenEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AuthTokenRepository : JpaRepository<AuthTokenEntity, String> {

    fun invalidateEmailVerificationTokens(user: UserEntity) {
        invalidateActiveTokens(user, AuthTokenType.EmailVerification)
    }

    fun invalidatePasswordResetTokens(user: UserEntity) {
        invalidateActiveTokens(user, AuthTokenType.PasswordReset)
    }

    fun deleteByExpiredAtBefore(now: Instant)

    @Modifying
    @Query(
        """
    UPDATE AuthTokenEntity entity
    SET entity.usedAt = CURRENT_TIMESTAMP
    WHERE entity.user = :user AND entity.tokenType = :tokenType
"""
    )
    fun invalidateActiveTokens(user: UserEntity, tokenType: AuthTokenType)
}
