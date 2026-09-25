package io.github.neronguyenvn.nerochat.domain.event

import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
sealed class ChatEvent {
    abstract val key: String
    abstract val exchangeName: String
    open val id: String = UUID.randomUUID().toString()
    open val occurredAt: Instant = Clock.System.now()
}
