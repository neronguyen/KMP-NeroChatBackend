package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.user.infra.database.model.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {

    fun findByUserIdAndHashedToken(userId: UUID, hashedToken: String): RefreshTokenEntity?

    fun deleteByUserIdAndHashedToken(userId: UUID, hashedToken: String)

    fun deleteByUserId(userId: UUID)
}
