package io.github.neronguyenvn.nerochat.user.api.request

import jakarta.validation.constraints.Email
import kotlinx.serialization.Serializable

@Serializable
data class EmailRequest(

    @field:Email
    val email: String
)

