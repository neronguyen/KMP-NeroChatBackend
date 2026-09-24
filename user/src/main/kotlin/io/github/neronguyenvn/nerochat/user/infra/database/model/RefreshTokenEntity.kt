package io.github.neronguyenvn.nerochat.user.infra.database.model

import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "refresh_tokens",
    schema = "user_service",
    indexes = [
        Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_refresh_tokens_user_token", columnList = "user_id,hashed_token"),
    ]
)
class RefreshTokenEntity private constructor(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var userId: UUID,

    @Column(nullable = false)
    var hashedToken: String,

    @Column(nullable = false)
    var expiredAt: Instant,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
) {
    constructor(
        userId: UserId,
        hashedToken: String,
        expiredAt: Instant,
    ) : this(
        userId = UUID.fromString(userId.value),
        hashedToken = hashedToken,
        expiredAt = expiredAt,
    )
}
