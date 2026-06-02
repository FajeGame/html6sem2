package com.example.lab3.application

import com.example.lab3.api.BadRequestException
import com.example.lab3.api.InvalidOrderStateException
import com.example.lab3.api.NotFoundException
import com.example.lab3.api.OrderCreationException
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import com.example.lab3.domain.Dish
import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderRepositoryPort
import com.example.lab3.domain.OrderStatus
import com.example.lab3.domain.User
import com.example.lab3.domain.UserRepositoryPort
import com.example.lab3.domain.event.OrderCreatedEvent
import com.example.lab3.domain.event.OrderStatusChangedEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.Mockito.lenient
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Callable
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {

    @Mock
    lateinit var orderRepositoryPort: OrderRepositoryPort

    @Mock
    lateinit var userRepositoryPort: UserRepositoryPort

    @Mock
    lateinit var dishService: DishService

    @Mock
    lateinit var orderEventPublisher: OrderEventPublisher

    @Mock
    lateinit var metrics: OrderMetrics

    @Mock
    lateinit var processingTimer: Timer

    @Mock
    lateinit var ordersCreatedCounter: Counter

    @InjectMocks
    lateinit var orderService: OrderService

    private val user = User(id = 1, email = "test@example.com", firstName = "Ivan", lastName = "Petrov", isActive = true)

    private val dish = Dish(
        id = 10,
        name = "Pizza",
        description = "Test",
        price = BigDecimal("499.0"),
        isAvailable = true,
        restaurantId = 1
    )

    private fun stubProcessingTimer() {
        whenever(metrics.processingDuration).thenReturn(processingTimer)
        lenient().whenever(metrics.ordersCreated).thenReturn(ordersCreatedCounter)
        doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val callable = invocation.arguments[0] as Callable<Order>
            callable.call()
        }.whenever(processingTimer).recordCallable(any<Callable<Order>>())
    }

    @Test
    fun `create создаёт заказ и публикует событие`() {
        stubProcessingTimer()
        val dishIds = listOf(10L)
        val savedOrder = Order(id = 1, userId = 1, status = OrderStatus.PENDING, createdAt = LocalDateTime.now(), dishIds = dishIds)
        whenever(userRepositoryPort.findById(1)).thenReturn(user)
        whenever(dishService.findByIds(dishIds)).thenReturn(listOf(dish))
        whenever(orderRepositoryPort.create(any())).thenReturn(savedOrder)

        val result = orderService.create(1, dishIds)

        assertEquals(savedOrder, result)
        verify(orderEventPublisher).publishOrderCreated(any())
    }

    @Test
    fun `create бросает OrderCreationException если блюдо недоступно`() {
        stubProcessingTimer()
        val unavailable = dish.copy(isAvailable = false)
        whenever(userRepositoryPort.findById(1)).thenReturn(user)
        whenever(dishService.findByIds(listOf(10L))).thenReturn(listOf(unavailable))

        assertThrows<OrderCreationException> {
            orderService.create(1, listOf(10L))
        }
    }

    @Test
    fun `create бросает BadRequestException при пустом списке блюд`() {
        stubProcessingTimer()
        assertThrows<BadRequestException> {
            orderService.create(1, emptyList())
        }
    }

    @Test
    fun `create бросает BadRequestException если пользователь не найден`() {
        stubProcessingTimer()
        whenever(userRepositoryPort.findById(999)).thenReturn(null)

        assertThrows<BadRequestException> {
            orderService.create(999, listOf(10L))
        }
    }

    @Test
    fun `create бросает BadRequestException если блюдо не найдено`() {
        stubProcessingTimer()
        whenever(userRepositoryPort.findById(1)).thenReturn(user)
        whenever(dishService.findByIds(listOf(10L, 11L))).thenReturn(listOf(dish))

        assertThrows<BadRequestException> {
            orderService.create(1, listOf(10L, 11L))
        }
    }

    @Test
    fun `updateStatus переводит заказ и публикует событие`() {
        val existing = Order(id = 1, userId = 1, status = OrderStatus.PENDING, createdAt = LocalDateTime.now(), dishIds = listOf(10L))
        val updated = existing.copy(status = OrderStatus.CONFIRMED)
        whenever(orderRepositoryPort.findById(1)).thenReturn(existing)
        whenever(orderRepositoryPort.update(updated)).thenReturn(updated)
        whenever(userRepositoryPort.findById(1)).thenReturn(user)

        val result = orderService.updateStatus(1, OrderStatus.CONFIRMED)

        assertEquals(OrderStatus.CONFIRMED, result.status)
        verify(orderEventPublisher).publishOrderStatusChanged(any())
    }

    @Test
    fun `updateStatus бросает NotFoundException если заказ не найден`() {
        whenever(orderRepositoryPort.findById(999)).thenReturn(null)

        assertThrows<NotFoundException> {
            orderService.updateStatus(999, OrderStatus.CONFIRMED)
        }
    }

    @Test
    fun `updateStatus бросает InvalidOrderStateException при недопустимом переходе`() {
        val existing = Order(id = 1, userId = 1, status = OrderStatus.DELIVERED, createdAt = LocalDateTime.now(), dishIds = listOf(10L))
        whenever(orderRepositoryPort.findById(1)).thenReturn(existing)

        assertThrows<InvalidOrderStateException> {
            orderService.updateStatus(1, OrderStatus.PENDING)
        }
    }
}
