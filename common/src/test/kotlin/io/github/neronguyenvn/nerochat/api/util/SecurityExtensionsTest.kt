package io.github.neronguyenvn.nerochat.api.util

import io.github.neronguyenvn.nerochat.domain.exception.UnauthorizedException
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class SecurityExtensionsTest {

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `requesterId returns userId when authenticated`() {
        val expectedUserId = UserId("user-123")
        val authentication = UsernamePasswordAuthenticationToken(expectedUserId, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        val actual = requesterId

        assertEquals(expectedUserId, actual)
    }

    @Test
    fun `requesterId throws UnauthorizedException when unauthenticated`() {
        SecurityContextHolder.clearContext()

        assertThrows<UnauthorizedException> {
            requesterId
        }
    }
}
