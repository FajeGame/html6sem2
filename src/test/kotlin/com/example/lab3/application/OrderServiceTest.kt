package com.example.lab3.application

import com.example.lab3.api.BadRequestException
import com.example.lab3.api.InvalidOrderStateException
import com.example.lab3.api.NotFoundException
import com.example.lab3.domain.Dish
import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderRepositoryPort
import com.example.lab3.domain.OrderStatus
import com.example.lab3.domain.User
import com.example.lab3.domain.UserRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
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

    @Test
    fun `create создаёт заказ при валидных данных`() {
        val dishIds = listOf(10L)
        val savedOrder = Order(id = 1, userId = 1, status = OrderStatus.PENDING, createdAt = LocalDateTime.now(), dishIds = dishIds)
        whenever(userRepositoryPort.findById(1)).thenReturn(user)
        whenever(dishService.findByIds(dishIds)).thenReturn(listOf(dish))
        whenever(orderRepositoryPort.create(any())).thenReturn(savedOrder)

        val result = orderService.create(1, dishIds)

        assertEquals(savedOrder, result)
    }

    @Test
    fun `create бросает BadRequestException при пустом списке блюд`() {
        assertThrows<BadRequestException> {
            orderService.create(1, emptyList())
        }
    }

    @Test
    fun `create бросает BadRequestException если пользователь не найден`() {
        whenever(userRepositoryPort.findById(999)).thenReturn(null)

        assertThrows<BadRequestException> {
            orderService.create(999, listOf(10L))
        }
    }

    @Test
    fun `create бросает BadRequestException если блюдо не найдено`() {
        whenever(userRepositoryPort.findById(1)).thenReturn(user)
        whenever(dishService.findByIds(listOf(10L, 11L))).thenReturn(listOf(dish))

        assertThrows<BadRequestException> {
            orderService.create(1, listOf(10L, 11L))
        }
    }

    @Test
    fun `updateStatus переводит заказ из PENDING в CONFIRMED`() {
        val existing = Order(id = 1, userId = 1, status = OrderStatus.PENDING, createdAt = LocalDateTime.now(), dishIds = listOf(10L))
        val updated = existing.copy(status = OrderStatus.CONFIRMED)
        whenever(orderRepositoryPort.findById(1)).thenReturn(existing)
        whenever(orderRepositoryPort.update(updated)).thenReturn(updated)

        val result = orderService.updateStatus(1, OrderStatus.CONFIRMED)

        assertEquals(OrderStatus.CONFIRMED, result.status)
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
