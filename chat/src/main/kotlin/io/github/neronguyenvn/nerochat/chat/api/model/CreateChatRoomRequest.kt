package io.github.neronguyenvn.nerochat.chat.api.model

import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRoomRequest(

    @field:Size(min = 1, message = "Chat rooms must have at least 2 unique participants")
    val otherUserIds: List<String>,

    val messageContent: String
)
