package io.github.neronguyenvn.nerochat.user.domain.model

import java.util.UUID

data class EmailVerificationToken(
    val id: UUID,
    val user: User,
)
