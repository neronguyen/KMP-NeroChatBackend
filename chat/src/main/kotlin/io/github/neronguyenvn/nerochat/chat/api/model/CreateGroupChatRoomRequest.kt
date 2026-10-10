package io.github.neronguyenvn.nerochat.chat.api.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import org.hibernate.validator.constraints.UUID

@Serializable
data class CreateGroupChatRoomRequest(
    @field:NotBlank(message = "Group name must not be blank")
    val name: String,

    @field:Size(min = 2, message = "Group chat rooms must have at least 2 participants")
    val participantIds: List<@UUID(message = "Participant ID must be a valid UUID") String>
)
