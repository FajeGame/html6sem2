package com.example.lab3.infrastructure.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "processed_events")
class ProcessedEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var orderId: Long = 0,

    @Column(nullable = false, length = 32)
    var newStatus: String = "",

    @Column(nullable = false)
    var processedAt: LocalDateTime = LocalDateTime.now()
)
