package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import kotlin.io.encoding.Base64

@Service
class JwtService(
    @param:Value($$"${jwt.secret}") private val secretBase64: String,
    @param:Value($$"${jwt.expiration-minutes}") private val expirationMinutes: Int,
) {
    val refreshTokenValidityMs = 30 * 24 * 60 * 60 * 1000L

    private val secretKey = Keys.hmacShaKeyFor(
        Base64.decode(secretBase64)
    )

    private val accessTokenValidityMs = expirationMinutes * 60 * 1000L

    fun generateAccessToken(userId: UUID): String {
        return generateToken(
            userId = userId,
            type = VALUE_CLAIMS_TYPE_ACCESS,
            expiry = accessTokenValidityMs
        )
    }

    fun generateRefreshToken(userId: UUID): String {
        return generateToken(
            userId = userId,
            type = VALUE_CLAIMS_TYPE_REFRESH,
            expiry = refreshTokenValidityMs
        )
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims[KEY_CLAIMS_TYPE] as? String ?: return false
        return tokenType == VALUE_CLAIMS_TYPE_ACCESS
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims[KEY_CLAIMS_TYPE] as? String ?: return false
        return tokenType == VALUE_CLAIMS_TYPE_REFRESH
    }

    fun getUserIdFromToken(token: String): UUID {
        val claims = parseAllClaims(token) ?: throw InvalidTokenException(
            message = "The attached JWT token is not valid"
        )
        return UUID.fromString(claims.subject)
    }

    private fun generateToken(
        userId: UUID,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        val algorithm = Jwts.SIG.HS256

        return Jwts.builder()
            .subject(userId.toString())
            .claim(KEY_CLAIMS_TYPE, type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, algorithm)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token

        val parser = Jwts.parser()
            .verifyWith(secretKey)
            .build()

        return runCatching {
            parser.parseSignedClaims(rawToken).payload
        }.getOrNull()
    }

    companion object {
        private const val KEY_CLAIMS_TYPE = "type"
        private const val VALUE_CLAIMS_TYPE_ACCESS = "access"
        private const val VALUE_CLAIMS_TYPE_REFRESH = "refresh"
    }
}
