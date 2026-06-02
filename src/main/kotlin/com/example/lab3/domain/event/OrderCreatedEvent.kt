package com.example.lab3.domain.event

import java.time.LocalDateTime

data class OrderCreatedEvent(
    val orderId: Long,
    val userId: Long,
    val dishIds: List<Long>,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
