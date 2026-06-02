package com.example.lab3.infrastructure.mock

import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderRepositoryPort
import com.example.lab3.domain.OrderStatus
import java.util.concurrent.ConcurrentHashMap

class MockOrderRepository : OrderRepositoryPort {
    private val storage = ConcurrentHashMap<Long, Order>()
    private var seq = 1L

    override fun create(order: Order): Order {
        val saved = order.copy(id = seq++)
        storage[saved.id] = saved
        return saved
    }

    override fun update(order: Order): Order? {
        if (!storage.containsKey(order.id)) return null
        storage[order.id] = order
        return order
    }

    override fun findById(id: Long): Order? = storage[id]

    override fun findAll(userId: Long?, status: OrderStatus?): List<Order> {
        return storage.values
            .asSequence()
            .filter { userId == null || it.userId == userId }
            .filter { status == null || it.status == status }
            .sortedBy { it.id }
            .toList()
    }

    override fun deleteById(id: Long): Boolean = storage.remove(id) != null
}
