package io.github.neronguyenvn.nerochat.user.infra.database.repository

import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.user.infra.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun findByEmail(email: String): UserEntity?
}

fun UserRepository.findByUserId(userId: UserId): UserEntity? =
    findByIdOrNull(UUID.fromString(userId.value))
