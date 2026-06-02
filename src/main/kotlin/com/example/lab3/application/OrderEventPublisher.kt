package com.example.lab3.application

import com.example.lab3.config.RabbitConfig
import com.example.lab3.domain.event.OrderCreatedEvent
import com.example.lab3.domain.event.OrderStatusChangedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class OrderEventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = KotlinLogging.logger {}

    fun publishOrderCreated(event: OrderCreatedEvent) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_CREATED_ROUTING_KEY,
            event
        )
        logger.info { "Published OrderCreated for orderId=${event.orderId}" }
    }

    fun publishOrderStatusChanged(event: OrderStatusChangedEvent) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_STATUS_ROUTING_KEY,
            event
        )
        logger.info { "Published OrderStatusChanged: ${event.oldStatus} -> ${event.newStatus} for orderId=${event.orderId}" }
    }
}
