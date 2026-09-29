package io.github.neronguyenvn.nerochat.chat.infra.database.model

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatParticipant
import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "chat_participants",
    schema = "chat_service",
    indexes = [
        Index(name = "idx_chat_participant_email", columnList = "email"),
    ]
)
class ChatParticipantEntity(
    @Id
    var userId: UUID,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var displayName: String,

    @Column(nullable = true)
    var profilePictureUrl: String? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)

fun ChatParticipantEntity.asExternalModel() = ChatParticipant(
    userId = UserId(userId.toString()),
    email = email,
    displayName = displayName,
    profilePictureUrl = profilePictureUrl,
)
