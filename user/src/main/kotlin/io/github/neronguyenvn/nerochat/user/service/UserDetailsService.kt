package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(id: String): UserDetails {
        val userId = UUID.fromString(id)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        return User.builder()
            .username(user.id.toString())
            .password(user.hashedPassword)
            .disabled(!user.isEmailVerified) // Optional: block login if email isn't verified
            .build()
    }
}