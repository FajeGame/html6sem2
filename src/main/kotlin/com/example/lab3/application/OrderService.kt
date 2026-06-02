package com.example.lab3.application

import com.example.lab3.api.BadRequestException
import com.example.lab3.api.InvalidOrderStateException
import com.example.lab3.api.NotFoundException
import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderRepositoryPort
import com.example.lab3.domain.OrderStatus
import com.example.lab3.domain.Dish
import com.example.lab3.domain.UserRepositoryPort
import com.example.lab3.domain.event.OrderCreatedEvent
import com.example.lab3.domain.event.OrderStatusChangedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepositoryPort: OrderRepositoryPort,
    private val userRepositoryPort: UserRepositoryPort,
    private val dishService: DishService,
    private val orderEventPublisher: OrderEventPublisher
) {
    private val logger = KotlinLogging.logger {}

    fun create(userId: Long, dishIds: List<Long>): Order {
        if (dishIds.isEmpty()) throw BadRequestException("dishIds must not be empty")
        val user = userRepositoryPort.findById(userId)
            ?: throw BadRequestException("User with id=$userId not found")
        val dishes = dishService.findByIds(dishIds)
        if (dishes.size != dishIds.distinct().size) throw BadRequestException("Some dishes not found")
        logger.info { "Creating order for userId=$userId" }
        val saved = orderRepositoryPort.create(
            Order(
                id = 0,
                userId = userId,
                status = OrderStatus.PENDING,
                createdAt = LocalDateTime.now(),
                dishIds = dishIds.distinct()
            )
        )
        orderEventPublisher.publishOrderCreated(
            OrderCreatedEvent(
                orderId = saved.id,
                userId = saved.userId,
                dishIds = saved.dishIds
            )
        )
        return saved
    }

    fun findById(id: Long): Order? = orderRepositoryPort.findById(id)

    fun findAll(userId: Long?, status: OrderStatus?): List<Order> = orderRepositoryPort.findAll(userId, status)

    fun findStuckPreparingOrders(createdBefore: LocalDateTime): List<Order> =
        orderRepositoryPort.findByStatusAndCreatedAtBefore(OrderStatus.PREPARING, createdBefore)

    fun deleteById(id: Long): Boolean = orderRepositoryPort.deleteById(id)

    fun updateStatus(id: Long, newStatus: OrderStatus): Order {
        val existing = orderRepositoryPort.findById(id) ?: throw NotFoundException("Order with id=$id not found")
        if (!isTransitionAllowed(existing.status, newStatus)) {
            throw InvalidOrderStateException("Invalid status transition from ${existing.status} to $newStatus")
        }
        if (existing.status == newStatus) return existing
        logger.info { "Updating order id=$id status=${existing.status}->$newStatus" }
        val updated = orderRepositoryPort.update(existing.copy(status = newStatus)) ?: existing
        val user = userRepositoryPort.findById(updated.userId)
            ?: throw NotFoundException("User with id=${updated.userId} not found")
        orderEventPublisher.publishOrderStatusChanged(
            OrderStatusChangedEvent(
                orderId = updated.id,
                userId = updated.userId,
                userEmail = user.email,
                oldStatus = existing.status,
                newStatus = newStatus
            )
        )
        return updated
    }

    fun getDishes(order: Order): List<Dish> = dishService.findByIds(order.dishIds)

    fun getOrderForUser(id: Long, userEmail: String, isAdmin: Boolean): Order {
        val order = findById(id) ?: throw NotFoundException("Order with id=$id not found")
        if (!isAdmin) {
            val user = userRepositoryPort.findByEmail(userEmail)
                ?: throw NotFoundException("User not found")
            if (order.userId != user.id) {
                throw AccessDeniedException("Access denied")
            }
        }
        return order
    }

    fun findAllForUser(userEmail: String, isAdmin: Boolean, status: OrderStatus?): List<Order> {
        if (isAdmin) return findAll(null, status)
        val user = userRepositoryPort.findByEmail(userEmail) ?: return emptyList()
        return findAll(user.id, status)
    }

    private fun isTransitionAllowed(from: OrderStatus, to: OrderStatus): Boolean {
        if (from == to) return true
        return when (from) {
            OrderStatus.PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED
            OrderStatus.CONFIRMED -> to == OrderStatus.PREPARING || to == OrderStatus.CANCELLED
            OrderStatus.PREPARING -> to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED
            OrderStatus.DELIVERED -> false
            OrderStatus.CANCELLED -> false
        }
    }
}
