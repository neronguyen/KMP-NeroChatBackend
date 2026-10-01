package io.github.neronguyenvn.nerochat.domain.type

import java.util.*

@JvmInline
value class ChatId(val value: String) {

    fun asUUID(): UUID {
        return UUID.fromString(value)
    }
}
