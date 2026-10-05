package io.github.neronguyenvn.nerochat.domain.type

import kotlinx.serialization.Serializable
import java.util.*

@JvmInline
@Serializable
value class ChatMessageId(val value: String) {

    fun asUUID(): UUID = UUID.fromString(value)
}
