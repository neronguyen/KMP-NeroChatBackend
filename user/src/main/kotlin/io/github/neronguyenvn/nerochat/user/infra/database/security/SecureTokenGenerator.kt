package io.github.neronguyenvn.nerochat.user.infra.database.security

import java.security.SecureRandom
import java.util.Base64

object SecureTokenGenerator {

    private const val TOKEN_BYTE_LENGTH = 32
    private val secureRandom = SecureRandom()

    fun generate(): String {
        val tokenBytes = ByteArray(TOKEN_BYTE_LENGTH)
        secureRandom.nextBytes(tokenBytes)

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(tokenBytes)
    }
}
