package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun findByEmail(email: String): UserEntity?

    fun findByEmailOrUsername(email: String, username: String): UserEntity?
}
