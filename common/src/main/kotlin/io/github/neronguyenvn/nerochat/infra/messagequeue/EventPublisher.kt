package io.github.neronguyenvn.nerochat.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.Event
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive
import org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO: Handle publisher confirms (correlated acks/nacks)
    fun publish(event: Event) {
        val transactionSynchronization = object : TransactionSynchronization {
            override fun afterCommit() {
                sendDirect(event)
            }
        }

        if (isActualTransactionActive()) {
            registerSynchronization(transactionSynchronization)
        } else {
            sendDirect(event)
        }
    }

    private fun sendDirect(event: Event) {
        try {
            rabbitTemplate.convertAndSend(
                event.exchangeName,
                event.key,
                event
            )
            logger.info("Successfully published event: ${event.key}")
        } catch (e: Exception) {
            logger.error("Failed to publish ${event.key} event", e)
            throw e
        }
    }
}
