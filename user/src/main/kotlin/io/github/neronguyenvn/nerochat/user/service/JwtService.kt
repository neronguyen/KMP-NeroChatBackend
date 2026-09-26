package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
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

    /** Returns a signed access JWT for [userId] with the configured lifetime in minutes. */
    fun generateAccessToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = VALUE_CLAIMS_TYPE_ACCESS,
            expiry = accessTokenValidityMs
        )
    }

    /** Returns a signed refresh JWT for [userId] with a 30-day lifetime. */
    fun generateRefreshToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = VALUE_CLAIMS_TYPE_REFRESH,
            expiry = refreshTokenValidityMs
        )
    }

    /**
     * Returns whether the JWT parses successfully and has the `access` type claim.
     * Accepts an optional exact `Bearer ` prefix. Parsing failures, including expiration or an
     * invalid signature, return false; the subject and stored token records are not checked.
     */
    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims[KEY_CLAIMS_TYPE] as? String ?: return false
        return tokenType == VALUE_CLAIMS_TYPE_ACCESS
    }

    /**
     * Returns whether the JWT parses successfully and has the `refresh` type claim.
     * Accepts an optional exact `Bearer ` prefix. Parsing failures, including expiration or an
     * invalid signature, return false; the subject and stored token records are not checked.
     */
    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims[KEY_CLAIMS_TYPE] as? String ?: return false
        return tokenType == VALUE_CLAIMS_TYPE_REFRESH
    }

    /**
     * Returns the subject from a signed JWT, accepting an optional exact `Bearer ` prefix.
     * Does not check the token type or validate that the subject is a UUID.
     *
     * @throws InvalidTokenException if parsing or signature/expiration validation fails.
     */
    fun getUserIdFromToken(token: String): UserId {
        val claims = parseAllClaims(token) ?: throw InvalidTokenException(
            message = "The attached JWT token is not valid"
        )
        return UserId(claims.subject)
    }

    /**
     * Returns an HS256-signed JWT with the user as subject and the supplied [type] claim.
     *
     * @param expiry token lifetime in milliseconds from the time of generation.
     */
    private fun generateToken(
        userId: UserId,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        val algorithm = Jwts.SIG.HS256

        return Jwts.builder()
            .subject(userId.value)
            .claim(KEY_CLAIMS_TYPE, type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, algorithm)
            .compact()
    }

    /**
     * Parses signed claims after removing an optional exact `Bearer ` prefix.
     * Returns null on any failure while parsing or validating the signed claims.
     */
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
