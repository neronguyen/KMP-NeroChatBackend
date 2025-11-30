package io.github.neronguyenvn.nerochat.user.infra.database.model

import io.github.neronguyenvn.nerochat.user.domain.model.AuthToken
import io.github.neronguyenvn.nerochat.user.domain.model.AuthTokenType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "auth_tokens",
    schema = "user_service",
)
data class AuthTokenEntity(

    @Id
    val token: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tokenType: AuthTokenType,

    @Column(nullable = false)
    val expiredAt: Instant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    val usedAt: Instant? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
) {
    val isUsed: Boolean
        get() = usedAt != null

    val isExpired: Boolean
        get() = Instant.now().isAfter(expiredAt)
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
