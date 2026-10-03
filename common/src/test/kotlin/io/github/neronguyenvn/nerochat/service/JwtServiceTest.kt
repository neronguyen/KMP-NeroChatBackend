package io.github.neronguyenvn.nerochat.service

import io.github.neronguyenvn.nerochat.domain.exception.InvalidTokenException
import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.io.encoding.Base64

class JwtServiceTest {

    private val secretBytes = "12345678901234567890123456789012".toByteArray()
    private val secretBase64 = Base64.encode(secretBytes)
    private val jwtService = JwtService(secretBase64 = secretBase64, expirationMinutes = 60)

    @Test
    fun `generate and validate access token`() {
        val userId = UserId(UUID.randomUUID().toString())
        val token = jwtService.generateAccessToken(userId)

        assertTrue(jwtService.validateAccessToken(token))
        assertFalse(jwtService.validateRefreshToken(token))
        assertEquals(userId, jwtService.getUserIdFromToken(token))
    }

    @Test
    fun `generate and validate refresh token`() {
        val userId = UserId(UUID.randomUUID().toString())
        val token = jwtService.generateRefreshToken(userId)

        assertTrue(jwtService.validateRefreshToken(token))
        assertFalse(jwtService.validateAccessToken(token))
        assertEquals(userId, jwtService.getUserIdFromToken(token))
    }

    @Test
    fun `invalid token throws InvalidTokenException`() {
        assertThrows<InvalidTokenException> {
            jwtService.getUserIdFromToken("invalid.token.here")
        }
    }
}
