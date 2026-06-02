package com.example.lab3.api

import com.example.lab3.domain.Dish
import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderStatus
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class OrderCreateRequest(
    @field:NotNull(message = "dishIds must not be empty")
    @field:NotEmpty(message = "dishIds must not be empty")
    val dishIds: List<Long>?
)

data class OrderStatusUpdateRequest(
    @field:NotNull(message = "status is required")
    val status: OrderStatus?
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val dishes: List<DishResponse>
)

fun Order.toResponse(dishes: List<Dish>): OrderResponse = OrderResponse(
    id = id,
    userId = userId,
    status = status,
    createdAt = createdAt,
    dishes = dishes.map { it.toResponse() }
)
