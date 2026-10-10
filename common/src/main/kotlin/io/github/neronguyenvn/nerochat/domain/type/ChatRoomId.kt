package io.github.neronguyenvn.nerochat.domain.type

import kotlinx.serialization.Serializable
import java.util.*

@JvmInline
@Serializable
value class ChatRoomId(val value: String) {

    constructor(uuid: UUID) : this(uuid.toString())

    fun asUUID(): UUID {
        return UUID.fromString(value)
    }
}
