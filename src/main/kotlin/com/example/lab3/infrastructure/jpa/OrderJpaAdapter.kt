package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderRepositoryPort
import com.example.lab3.domain.OrderStatus

class OrderJpaAdapter(
    private val orderJpaRepository: OrderJpaRepository,
    private val userJpaRepository: UserJpaRepository,
    private val dishJpaRepository: DishJpaRepository
) : OrderRepositoryPort {
    override fun create(order: Order): Order {
        val user = userJpaRepository.findById(order.userId).orElseThrow()
        val dishes = dishJpaRepository.findAllByIdIn(order.dishIds).toMutableSet()
        val entity = OrderEntity(
            id = order.id,
            user = user,
            status = order.status,
            createdAt = order.createdAt,
            dishes = dishes
        )
        return orderJpaRepository.save(entity).toDomain()
    }

    override fun update(order: Order): Order? {
        if (!orderJpaRepository.existsById(order.id)) return null
        val user = userJpaRepository.findById(order.userId).orElseThrow()
        val dishes = dishJpaRepository.findAllByIdIn(order.dishIds).toMutableSet()
        val entity = OrderEntity(
            id = order.id,
            user = user,
            status = order.status,
            createdAt = order.createdAt,
            dishes = dishes
        )
        return orderJpaRepository.save(entity).toDomain()
    }

    override fun findById(id: Long): Order? = orderJpaRepository.findDetailedById(id)?.toDomain()

    override fun findAll(userId: Long?, status: OrderStatus?): List<Order> {
        val entities = when {
            userId != null && status != null -> orderJpaRepository.findAllByUserIdAndStatus(userId, status)
            userId != null -> orderJpaRepository.findAllByUserId(userId)
            status != null -> orderJpaRepository.findAllByStatus(status)
            else -> orderJpaRepository.findAllByOrderByIdAsc()
        }
        return entities.map { it.toDomain() }.sortedBy { it.id }
    }

    override fun findByStatusAndCreatedAtBefore(status: OrderStatus, createdBefore: java.time.LocalDateTime): List<Order> =
        orderJpaRepository.findByStatusAndCreatedAtBefore(status, createdBefore)
            .map { it.toDomain() }
            .sortedBy { it.id }

    override fun deleteById(id: Long): Boolean {
        if (!orderJpaRepository.existsById(id)) return false
        orderJpaRepository.deleteById(id)
        return true
    }
}
