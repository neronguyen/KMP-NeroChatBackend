package io.github.neronguyenvn.nerochat.user.api.request

import io.github.neronguyenvn.nerochat.user.api.validation.Password
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(

    @field:Email(message = "Email must be a valid email address")
    val email: String,

    @field:NotBlank
    val displayName: String,

    @field:Password
    val password: String,
)