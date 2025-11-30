package io.github.neronguyenvn.nerochat.user.api.request

import io.github.neronguyenvn.nerochat.user.api.validation.Password
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(

    @field:NotBlank
    val accessToken: String,

    @field:NotBlank
    val oldPassword: String,

    @field:Password
    val newPassword: String
)

