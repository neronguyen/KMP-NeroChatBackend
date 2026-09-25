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

    @Bean
    fun messageConverter(): KotlinSerializationJsonAmqpMessageConverter {
        return KotlinSerializationJsonAmqpMessageConverter()
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: KotlinSerializationJsonAmqpMessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun exchangeUserEvents() = TopicExchange(
        UserEvent.EXCHANGE_NAME,
        true,
        false
    )

    @Bean
    fun queueUserEvents() = Queue(
        UserEvent.QUEUE_NAME,
        true
    )

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
