package io.github.neronguyenvn.nerochat.domain.type

import java.util.*

@JvmInline
value class ChatMessageId(val value: String) {

    fun asUUID(): UUID = UUID.fromString(value)
}
