package io.github.neronguyenvn.nerochat.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.ChatEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Sends [event] to its exchange using its routing key.
     * Publishing failures are logged and are not propagated to the caller.
     */
    fun <T: ChatEvent> publish(event: T) {
        try {
            rabbitTemplate.convertAndSend(
                event.exchangeName,
                event.key,
                event
            )
            logger.info("Successfully published event: ${event.key}")
        } catch(e: Exception) {
            logger.error("Failed to publish ${event.key} event", e)
        }
    }
}
