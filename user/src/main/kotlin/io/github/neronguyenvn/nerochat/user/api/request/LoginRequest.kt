package io.github.neronguyenvn.nerochat.user.api.request

import io.github.neronguyenvn.nerochat.user.api.validation.Password
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(

    @field:NotBlank
    @field:Email
    val email: String,

    @field:Password
    val password: String,
)
