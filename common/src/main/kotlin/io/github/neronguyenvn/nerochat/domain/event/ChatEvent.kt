package io.github.neronguyenvn.nerochat.domain.event

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
abstract class ChatEvent {
    abstract val id: String
    abstract val key: String
    abstract val occurredAt: Instant
    abstract val exchange: String
}
