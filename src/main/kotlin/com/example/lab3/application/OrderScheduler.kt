package com.example.lab3.application

import com.example.lab3.domain.OrderStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("!test")
class OrderScheduler(
    private val orderService: OrderService,
    @Value("\${app.scheduler.stuck-order-threshold-hours}") private val thresholdHours: Long
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedDelayString = "\${app.scheduler.stuck-order-interval-ms}")
    fun cancelStuckOrders() {
        val threshold = LocalDateTime.now().minusHours(thresholdHours)
        val stuck = orderService.findStuckPreparingOrders(threshold)
        logger.info { "Found ${stuck.size} stuck orders in PREPARING status" }
        stuck.forEach { order ->
            orderService.updateStatus(order.id, OrderStatus.CANCELLED)
            logger.info { "Cancelled stuck order id=${order.id}" }
        }
    }
}
