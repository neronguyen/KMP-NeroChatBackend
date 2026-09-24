package io.github.neronguyenvn.nerochat.infra.messagequeue

import io.github.neronguyenvn.nerochat.domain.event.user.UserEvent
import kotlinx.serialization.json.Json
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
    ): RabbitTemplate {
        val messageConverter = KotlinSerializationJsonAmqpMessageConverter(
            Json {
                classDiscriminator = EVENT_TYPE
            }
        )
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun userExchange() = TopicExchange(
        UserEvent.EXCHANGE,
        true,
        false
    )

    @Bean
    fun userEventsQueue() = Queue(
        QUEUE_USER_EVENTS,
        true
    )

    companion object {
        const val EVENT_TYPE = "type"
        const val QUEUE_USER_EVENTS = "user.events"
    }
}
