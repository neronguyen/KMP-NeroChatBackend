package io.github.neronguyenvn.nerochat.chat.infra.database.repository

import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatMessageEntity
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
        JOIN FETCH m.sender
        WHERE m.chatRoomId = :chatRoomId
        AND (:before IS NULL OR m.createdAt < :before) 
        ORDER BY m.createdAt DESC
    """
    )
    fun findByChatRoomIdBefore(
        chatRoomId: UUID,
        before: Instant?,
        pageable: Pageable
    ): Slice<ChatMessageEntity>

    @Query(
        """
        SELECT m
        FROM ChatMessageEntity m
        LEFT JOIN FETCH m.sender
        WHERE m.chatRoomId IN :chatRoomIds
        AND (m.createdAt, m.id) = (
            SELECT m2.createdAt, m2.id
            FROM ChatMessageEntity m2
            WHERE m2.chatRoomId = m.chatRoomId
            ORDER BY m2.createdAt DESC 
            LIMIT 1
        )
    """
    )
    fun findLatestMessagesByChatRoomIds(chatRoomIds: Set<UUID>): List<ChatMessageEntity>
}
