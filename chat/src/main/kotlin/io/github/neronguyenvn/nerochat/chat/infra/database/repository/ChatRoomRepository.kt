package io.github.neronguyenvn.nerochat.chat.infra.database.repository

import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatRoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ChatRoomRepository : JpaRepository<ChatRoomEntity, UUID> {

    @Query(
        """
        SELECT c
        FROM ChatRoomEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE c.id = :id
        AND EXISTS (
            SELECT 1
            FROM c.participants p
            WHERE p.userId = :userId
        )
    """
    )
    fun findChatRoomById(id: UUID, userId: UUID): ChatRoomEntity?

    @Query(
        """
        SELECT c
        FROM ChatRoomEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE EXISTS (
            SELECT 1
            FROM c.participants p
            WHERE p.userId = :userId
        )
    """
    )
    fun findAllByUserId(userId: UUID): List<ChatRoomEntity>

    @Query(
        """
        SELECT DISTINCT c
        FROM ChatRoomEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE c.directPairKey = :directPairKey
    """
    )
    fun findDirectChatRoomBetween(
        directPairKey: String
    ): ChatRoomEntity?
}
