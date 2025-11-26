package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.domain.exception.UserAlreadyExistsException
import io.github.neronguyenvn.nerochat.user.domain.model.User
import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import io.github.neronguyenvn.nerochat.user.infra.database.model.asExternalModel
import io.github.neronguyenvn.nerochat.user.infra.database.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun register(
        email: String,
        password: String,
    ): User {
        val existing = userRepository.findByEmail(email = email)

        if (existing != null) {
            throw UserAlreadyExistsException()
        }

        val saved = userRepository.save(
            UserEntity(
                email = email,
                hashedPassword = passwordEncoder.encode(password)!!,
            )
        )

        return saved.asExternalModel()
    }
}
