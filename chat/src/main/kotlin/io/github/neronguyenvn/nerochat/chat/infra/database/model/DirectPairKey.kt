package io.github.neronguyenvn.nerochat.chat.infra.database.model

import io.github.neronguyenvn.nerochat.domain.type.UserId
import java.util.UUID

@JvmInline
value class DirectPairKey(val value: String) {
    companion object {
        fun of(userA: UUID, userB: UUID): DirectPairKey {
            val u1 = userA.toString()
            val u2 = userB.toString()
            val key = if (u1 < u2) "${u1}_$u2" else "${u2}_$u1"
            return DirectPairKey(key)
        }

        fun of(userA: UserId, userB: UserId): DirectPairKey = of(userA.asUUID(), userB.asUUID())
    }
}
