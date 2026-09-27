package io.github.neronguyenvn.nerochat.user.api.request

import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(

    @field:NotBlank
    val refreshToken: String
)
