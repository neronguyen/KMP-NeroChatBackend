package io.github.neronguyenvn.nerochat.user.api.request

import io.github.neronguyenvn.nerochat.user.api.validation.Password
import jakarta.validation.constraints.Email
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(

    @field:Email(message = "Email must be a valid email address")
    val email: String,

    @field:Password
    val password: String,
)
