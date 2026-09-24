package io.github.neronguyenvn.nerochat.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.ChatEvent
import kotlinx.serialization.json.Json
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.support.converter.MessageConverter

class KotlinSerializationJsonAmqpMessageConverter(
    private val json: Json,
) : MessageConverter {

    override fun toMessage(
        `object`: Any,
        messageProperties: MessageProperties
    ): Message {
        val body = when (`object`) {
            is ChatEvent -> json.encodeToString(`object`)
            else -> error("Unsupported message type: ${`object`::class}")
        }

        return Message(
            body.encodeToByteArray(),
            messageProperties
        )
    }

    override fun fromMessage(message: Message): Any {
        return json.decodeFromString<ChatEvent>(
            message.body.decodeToString()
        )
    }
}
