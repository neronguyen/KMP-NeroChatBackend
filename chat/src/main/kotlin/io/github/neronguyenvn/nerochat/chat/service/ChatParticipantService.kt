package io.github.neronguyenvn.nerochat.chat.service

import io.github.neronguyenvn.nerochat.chat.domain.model.ChatParticipant
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatParticipantEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.ChatParticipantRepository
import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.domain.util.normalizeEmail
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatParticipantService(private val chatParticipantRepository: ChatParticipantRepository) {

    fun createChatParticipant(chatParticipant: ChatParticipant) {
        chatParticipantRepository.save(
            ChatParticipantEntity(
                userId = UUID.fromString(chatParticipant.userId.value),
                email = chatParticipant.email,
                displayName = chatParticipant.displayName
            )
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        val userIdByUuid = UUID.fromString(userId.value)
        return chatParticipantRepository.findByIdOrNull(userIdByUuid)?.asExternalModel()
    }

    fun findChatParticipantByEmail(email: String): ChatParticipant? {
        val normalizedEmail = normalizeEmail(email)
        return chatParticipantRepository.findByEmail(normalizedEmail)?.asExternalModel()
    }
}
