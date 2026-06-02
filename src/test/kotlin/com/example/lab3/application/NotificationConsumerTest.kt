package com.example.lab3.application

import com.example.lab3.domain.OrderStatus
import com.example.lab3.domain.event.OrderStatusChangedEvent
import com.example.lab3.infrastructure.jpa.ProcessedEventEntity
import com.example.lab3.infrastructure.jpa.ProcessedEventRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class NotificationConsumerTest {

    @Mock
    lateinit var notificationService: NotificationService

    @Mock
    lateinit var processedEventRepository: ProcessedEventRepository

    @InjectMocks
    lateinit var notificationConsumer: NotificationConsumer

    private val event = OrderStatusChangedEvent(
        orderId = 1,
        userId = 1,
        userEmail = "user@test.com",
        oldStatus = OrderStatus.PENDING,
        newStatus = OrderStatus.CONFIRMED
    )

    @Test
    fun `handleOrderStatusChanged отправляет email при новом событии`() {
        whenever(processedEventRepository.existsByOrderIdAndNewStatus(1, "CONFIRMED")).thenReturn(false)

        notificationConsumer.handleOrderStatusChanged(event)

        verify(notificationService).sendStatusChangedEmail(event)
        verify(processedEventRepository).save(org.mockito.kotlin.any())
    }

    @Test
    fun `handleOrderStatusChanged пропускает дубль`() {
        whenever(processedEventRepository.existsByOrderIdAndNewStatus(1, "CONFIRMED")).thenReturn(true)

        notificationConsumer.handleOrderStatusChanged(event)

        verify(notificationService, never()).sendStatusChangedEmail(event)
        verify(processedEventRepository, never()).save(org.mockito.kotlin.any())
    }
}
