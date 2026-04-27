package io.github.neronguyenvn.nerochat.user.infra.database.model

import io.github.neronguyenvn.nerochat.user.domain.model.AuthToken
import io.github.neronguyenvn.nerochat.user.domain.model.AuthTokenType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "auth_tokens",
    schema = "user_service",
)
class AuthTokenEntity(

    @Id
    var token: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var tokenType: AuthTokenType,

    @Column(nullable = false)
    var expiredAt: Instant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    var usedAt: Instant? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
) {
    fun isUsed() = usedAt != null

    fun isExpired() = Instant.now().isAfter(expiredAt)
}

fun AuthTokenEntity.asEmailVerificationToken(): AuthToken.EmailVerification {
    if (tokenType != AuthTokenType.EmailVerification) error("Invalid token type")
    return AuthToken.EmailVerification(
        token = token,
        user = user.asExternalModel()
    )
}

fun AuthTokenEntity.asPasswordResetToken(): AuthToken.PasswordReset {
    if (tokenType != AuthTokenType.PasswordReset) error("Invalid token type")
    return AuthToken.PasswordReset(
        token = token,
        user = user.asExternalModel()
    )
}