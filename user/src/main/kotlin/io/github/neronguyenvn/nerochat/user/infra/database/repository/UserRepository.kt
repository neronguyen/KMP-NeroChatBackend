package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {

    /** Returns the user matching [email], or null when absent. */
    fun findByEmail(email: String): UserEntity?
}

/**
 * Returns the user matching the UUID in [userId], or null when absent.
 *
 * @throws IllegalArgumentException if [userId] cannot be parsed as a UUID.
 */
fun UserRepository.findByUserId(userId: UserId): UserEntity? =
    findByIdOrNull(UUID.fromString(userId.value))
