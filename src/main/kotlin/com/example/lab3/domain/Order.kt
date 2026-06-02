package com.example.lab3.domain

import java.time.LocalDateTime

data class Order(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val dishIds: List<Long>
)
