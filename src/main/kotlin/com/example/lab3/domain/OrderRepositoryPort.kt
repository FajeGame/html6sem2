package com.example.lab3.domain

interface OrderRepositoryPort {
    fun create(order: Order): Order
    fun update(order: Order): Order?
    fun findById(id: Long): Order?
    fun findAll(userId: Long?, status: OrderStatus?): List<Order>
    fun findByStatusAndCreatedAtBefore(status: OrderStatus, createdBefore: java.time.LocalDateTime): List<Order>
    fun deleteById(id: Long): Boolean
}
