package io.github.neronguyenvn.nerochat.api.util

import io.github.neronguyenvn.nerochat.domain.exception.UnauthorizedException
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requesterId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()
