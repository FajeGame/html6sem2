package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.OrderStatus
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findDetailedById(id: Long): OrderEntity?

    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findAllByUserId(userId: Long): List<OrderEntity>

    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findAllByStatus(status: OrderStatus): List<OrderEntity>

    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findAllByUserIdAndStatus(userId: Long, status: OrderStatus): List<OrderEntity>

    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findAllByOrderByIdAsc(): List<OrderEntity>
}
