package io.github.neronguyenvn.nerochat.chat.infra.database.repository

import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ChatParticipantRepository : JpaRepository<ChatParticipantEntity, UUID> {

    fun findByUserIdIn(userIds: Set<UUID>): Set<ChatParticipantEntity>

    fun findByEmail(email: String): ChatParticipantEntity?
}
