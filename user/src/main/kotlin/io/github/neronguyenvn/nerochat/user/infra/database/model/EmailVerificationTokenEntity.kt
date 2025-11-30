package io.github.neronguyenvn.nerochat.user.infra.database.model

import io.github.neronguyenvn.nerochat.user.domain.model.EmailVerificationToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "email_verification_tokens",
    schema = "user_service",
)
data class EmailVerificationTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

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

fun EmailVerificationTokenEntity.asExternalModel() = EmailVerificationToken(
    id = id!!,
    user = user.asExternalModel(),
)
