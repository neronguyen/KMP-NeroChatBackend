package io.github.neronguyenvn.nerochat.chat.api.model

import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class CreateDirectChatRoomRequest(
    @field:NotBlank(message = "Target user ID must not be blank")
    val targetUserId: String,

    @field:NotBlank(message = "Message must not be blank")
    val message: String
)
