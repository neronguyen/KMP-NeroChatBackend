package io.github.neronguyenvn.nerochat.user.api.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class EmailRequest(

    @field:NotBlank
    @field:Email
    val email: String
)

