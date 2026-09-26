package io.github.neronguyenvn.nerochat.user.infra.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoderConfig {

    /** Provides BCrypt hashing and verification with the encoder's default strength. */
    @Bean
    fun providesPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
