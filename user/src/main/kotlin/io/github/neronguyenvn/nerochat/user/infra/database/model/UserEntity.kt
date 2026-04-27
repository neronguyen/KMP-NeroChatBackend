package io.github.neronguyenvn.nerochat.user.infra.database.model

import io.github.neronguyenvn.nerochat.user.domain.model.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "users",
    schema = "user_service",
    indexes = [
        Index(name = "idx_users_email", columnList = "email"),
    ]
)
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(unique = true, nullable = false)
    var email: String,

    @Column(nullable = false)
    var displayName: String,

    @Column(nullable = false)
    var hashedPassword: String,

    var isEmailVerified: Boolean = false,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
)

fun UserEntity.asExternalModel(): User {
    return User(
        id = id!!,
        email = email,
        isEmailVerified = isEmailVerified
    )
}
