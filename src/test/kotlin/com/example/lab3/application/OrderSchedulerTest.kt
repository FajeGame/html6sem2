package com.example.lab3.application

import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OrderSchedulerTest {

    @Mock
    lateinit var orderService: OrderService

    private lateinit var orderScheduler: OrderScheduler

    @BeforeEach
    fun setUp() {
        orderScheduler = OrderScheduler(orderService, thresholdHours = 1)
    }

    @Test
    fun `cancelStuckOrders отменяет зависшие заказы`() {
        val stuckOrder = Order(
            id = 1,
            userId = 1,
            status = OrderStatus.PREPARING,
            createdAt = LocalDateTime.now().minusHours(2),
            dishIds = listOf(10L)
        )
        whenever(orderService.findStuckPreparingOrders(any())).thenReturn(listOf(stuckOrder))
        whenever(orderService.updateStatus(1, OrderStatus.CANCELLED)).thenReturn(stuckOrder.copy(status = OrderStatus.CANCELLED))

        orderScheduler.cancelStuckOrders()

        verify(orderService).findStuckPreparingOrders(any())
        verify(orderService).updateStatus(1, OrderStatus.CANCELLED)
    }

    @Test
    fun `cancelStuckOrders ничего не делает если зависших заказов нет`() {
        whenever(orderService.findStuckPreparingOrders(any())).thenReturn(emptyList())

        orderScheduler.cancelStuckOrders()

        verify(orderService).findStuckPreparingOrders(any())
        verify(orderService, times(0)).updateStatus(any(), any())
    }
}
