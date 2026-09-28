package io.github.neronguyenvn.nerochat.chat.infra.database.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "chat_messages",
    schema = "chat_service",
    indexes = [
        Index(
            name = "idx_chat_message_chat_id_created_at",
            columnList = "chat_id,created_at DESC"
        )
    ]
)
class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var content: String,

    @Column(name = "chat_id", nullable = false, updatable = false)
    var chatId: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false, insertable = false, updatable = false)
    var chat: ChatEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, insertable = false, updatable = false)
    var sender: ChatParticipantEntity,

    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)
