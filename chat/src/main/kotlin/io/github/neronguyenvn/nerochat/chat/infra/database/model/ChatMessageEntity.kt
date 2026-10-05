package io.github.neronguyenvn.nerochat.chat.infra.database.model

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatMessage
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.ChatMessageId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinInstant

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
    var chat: ChatEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    var sender: ChatParticipantEntity,

    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)

@OptIn(ExperimentalTime::class)
fun ChatMessageEntity.asExternalModel() = ChatMessage(
    id = ChatMessageId(id?.toString() ?: error("ChatMessageId have to be generated")),
    chatId = ChatId(chatId.toString()),
    content = content,
    sender = sender.asExternalModel(),
    createdAt = createdAt.toKotlinInstant()
)
