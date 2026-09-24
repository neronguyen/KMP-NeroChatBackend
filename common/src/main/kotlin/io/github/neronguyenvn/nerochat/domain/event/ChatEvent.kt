package io.github.neronguyenvn.nerochat.domain.event

import java.time.Instant

abstract class ChatEvent {
    abstract val id: String
    abstract val key: String
    abstract val occurredAt: Instant
    abstract val exchange: String
}
