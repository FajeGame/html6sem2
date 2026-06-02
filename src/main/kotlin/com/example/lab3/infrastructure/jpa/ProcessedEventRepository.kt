package com.example.lab3.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedEventRepository : JpaRepository<ProcessedEventEntity, Long> {
    fun existsByOrderIdAndNewStatus(orderId: Long, newStatus: String): Boolean
}
