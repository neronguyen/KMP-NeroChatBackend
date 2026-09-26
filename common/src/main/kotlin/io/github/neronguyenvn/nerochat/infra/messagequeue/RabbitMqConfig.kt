package io.github.neronguyenvn.nerochat.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.UserEvent
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

    /** Creates the JSON converter used to serialize and deserialize chat events. */
    @Bean
    fun messageConverter(): KotlinSerializationJsonAmqpMessageConverter {
        return KotlinSerializationJsonAmqpMessageConverter()
    }

    /** Creates a RabbitMQ template using [connectionFactory] and the event [messageConverter]. */
    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: KotlinSerializationJsonAmqpMessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    /** Declares a durable user-event topic exchange that is not automatically deleted. */
    @Bean
    fun exchangeUserEvents() = TopicExchange(
        UserEvent.EXCHANGE_NAME,
        true,
        false
    )

    /** Declares a durable queue for user events. */
    @Bean
    fun queueUserEvents() = Queue(
        UserEvent.QUEUE_NAME,
        true
    )

    /** Routes events matching [UserEvent.ROUTING_KEY_PATTERN] from the exchange to the queue. */
    @Bean
    fun bindingUserEvents(
        queueUserEvents: Queue,
        exchangeUserEvents: TopicExchange,
    ): Binding {
        return BindingBuilder
            .bind(queueUserEvents)
            .to(exchangeUserEvents)
            .with(UserEvent.ROUTING_KEY_PATTERN)
    }
}
