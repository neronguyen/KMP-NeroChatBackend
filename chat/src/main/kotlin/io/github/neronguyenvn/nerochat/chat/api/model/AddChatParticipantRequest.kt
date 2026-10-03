package io.github.neronguyenvn.nerochat.chat.api.model

import io.github.neronguyenvn.nerochat.domain.type.UserId
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class AddChatParticipantRequest(

    @field:Size(min = 1)
    val userIds: List<UserId>
)
