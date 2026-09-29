package io.github.neronguyenvn.nerochat.chat.service

import io.github.neronguyenvn.nerochat.chat.domain.exception.ChatParticipantNotFoundException
import io.github.neronguyenvn.nerochat.chat.domain.exception.InvalidChatSizeException
import io.github.neronguyenvn.nerochat.chat.domain.model.Chat
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.ChatMessageEntity
import io.github.neronguyenvn.nerochat.chat.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.chat.infra.database.repository.*
import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository
) {

    @Transactional
    fun createChat(
        messageContent: String,
        creatorId: UserId,
        otherUserIds: Set<UserId>
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByUserId(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        val savedChat = chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = otherParticipants
            )
        )

        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                chatId = savedChat.id ?: error("ChatId have to be generated"),
                sender = creator,
                content = messageContent
            )
        )

        return savedChat.asExternalModel(savedMessage.asExternalModel())
    }
}
