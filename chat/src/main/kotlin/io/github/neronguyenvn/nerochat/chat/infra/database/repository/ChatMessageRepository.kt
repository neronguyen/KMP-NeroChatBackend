package io.github.neronguyenvn.nerochat.chat.infra.database.repository

import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatMessageEntity
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.*

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, UUID> {

    @Query(
        """
        SELECT m
        FROM ChatMessageEntity m
        WHERE m.chatId = :chatId
        AND m.createdAt < :before
        ORDER BY m.createdAt DESC
    """
    )
    fun findByChatIdBefore(
        chatId: UUID,
        before: Instant,
        pageable: Pageable
    ): Slice<ChatMessageEntity>

    @Query(
        """
        SELECT m
        FROM ChatMessageEntity m
        LEFT JOIN FETCH m.sender
        WHERE m.chatId IN :chatIds
        AND (m.createdAt, m.id) = (
            SELECT m2.createdAt, m2.id
            FROM ChatMessageEntity m2
            WHERE m2.chatId = m.chatId
            ORDER BY m2.createdAt DESC 
            LIMIT 1
        )
    """
    )
    fun findLatestMessagesByChatIds(chatIds: Set<UUID>): List<ChatMessageEntity>
}

fun ChatMessageRepository.findByChatIdBefore(
    chatId: ChatId,
    before: Instant,
    pageable: Pageable
): Slice<ChatMessageEntity> {
    val uuid = UUID.fromString(chatId.value)
    return findByChatIdBefore(uuid, before, pageable)
}

fun ChatMessageRepository.findLatestMessagesByChatIds(chatIds: Set<ChatId>): List<ChatMessageEntity> {
    val setUuid = chatIds.map { UUID.fromString(it.value) }.toSet()
    return findLatestMessagesByChatIds(setUuid)
}
