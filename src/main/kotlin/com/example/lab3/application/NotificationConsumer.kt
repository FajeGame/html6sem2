package com.example.lab3.application

import com.example.lab3.config.RabbitConfig
import com.example.lab3.domain.event.OrderCreatedEvent
import com.example.lab3.domain.event.OrderStatusChangedEvent
import com.example.lab3.infrastructure.jpa.ProcessedEventRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationConsumer(
    private val notificationService: NotificationService,
    private val processedEventRepository: ProcessedEventRepository
) {
    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = [RabbitConfig.ORDER_STATUS_QUEUE])
    fun handleOrderStatusChanged(event: OrderStatusChangedEvent) {
        if (processedEventRepository.existsByOrderIdAndNewStatus(event.orderId, event.newStatus.name)) {
            logger.warn { "Duplicate event for order ${event.orderId} status ${event.newStatus}, skipping" }
            return
        }
        logger.info { "Received OrderStatusChanged: order ${event.orderId} -> ${event.newStatus}" }
        notificationService.sendStatusChangedEmail(event)
        processedEventRepository.save(
            com.example.lab3.infrastructure.jpa.ProcessedEventEntity(
                orderId = event.orderId,
                newStatus = event.newStatus.name
            )
        )
    }

    @RabbitListener(queues = [RabbitConfig.ORDER_CREATED_QUEUE])
    fun handleOrderCreated(event: OrderCreatedEvent) {
        logger.info { "New order ${event.orderId} from user ${event.userId}, dishes=${event.dishIds}" }
    }
}
