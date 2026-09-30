package io.github.neronguyenvn.nerochat.chat.api.model

import jakarta.validation.constraints.Size

data class CreateChatRequest(

    @field:Size(min = 1, message = "Chats must have at least 2 unique participants")
    val otherUserIds: List<String>,

    val messageContent: String
)
