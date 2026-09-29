package io.github.neronguyenvn.nerochat.chat.infra.database.repository

import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatParticipantEntity
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import java.util.*

interface ChatParticipantRepository : JpaRepository<ChatParticipantEntity, UUID> {

    fun findByUserIdIn(userIds: List<UUID>): Set<ChatParticipantEntity>

    fun findByEmail(email: String): ChatParticipantEntity?
}

fun ChatParticipantRepository.findByUserIdIn(userIds: Set<UserId>): Set<ChatParticipantEntity> {
    val userIds = userIds.map { UUID.fromString(it.value) }
    return findByUserIdIn(userIds)
}

fun ChatParticipantRepository.findByUserId(userId: UserId): ChatParticipantEntity? {
    return findByIdOrNull(UUID.fromString(userId.value))
}
