package io.github.neronguyenvn.nerochat.chat.infra.database.repository

import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatEntity
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ChatRepository : JpaRepository<ChatEntity, UUID> {

    @Query(
        """
        SELECT c
        FROM ChatEntity c
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
    fun findChatById(id: UUID, userId: UUID): ChatEntity?

    @Query(
        """
        SELECT c
        FROM ChatEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE EXISTS (
            SELECT 1
            FROM c.participants p
            WHERE p.userId = :userId
        )
    """
    )
    fun findAllByUserId(userId: UUID): List<ChatEntity>
}

fun ChatRepository.findChatById(id: ChatId, userId: UserId): ChatEntity? {
    val uuid = UUID.fromString(id.value)
    val userUuid = UUID.fromString(userId.value)
    return findChatById(uuid, userUuid)
}

fun ChatRepository.findAllByUserId(userId: UserId): List<ChatEntity> {
    val userUuid = UUID.fromString(userId.value)
    return findAllByUserId(userUuid)
}
