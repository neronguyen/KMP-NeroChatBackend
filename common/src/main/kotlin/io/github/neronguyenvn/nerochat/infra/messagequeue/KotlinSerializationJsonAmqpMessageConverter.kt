package io.github.neronguyenvn.nerochat.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.ChatEvent
import kotlinx.serialization.json.Json
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.support.converter.MessageConverter

class KotlinSerializationJsonAmqpMessageConverter(
    private val json: Json = defaultJson
) : MessageConverter {

    /**
     * Encodes a [ChatEvent] as UTF-8 JSON, retaining the supplied [messageProperties].
     *
     * @throws IllegalStateException if the object is not a [ChatEvent].
     */
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

    /**
     * Decodes the UTF-8 JSON body of [message] into a [ChatEvent].
     * Deserialization failures are propagated to the caller.
     */
    override fun fromMessage(message: Message): Any {
        return json.decodeFromString<ChatEvent>(
            message.body.decodeToString()
        )
    }

    private companion object {
        const val EVENT_TYPE = "type"

        val defaultJson = Json {
            classDiscriminator = EVENT_TYPE
        }
    }
}
