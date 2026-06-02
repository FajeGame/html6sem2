package com.example.lab3.application

import com.example.lab3.domain.Dish
import com.example.lab3.domain.DishRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class DishServiceTest {

    @Mock
    lateinit var dishRepositoryPort: DishRepositoryPort

    @InjectMocks
    lateinit var dishService: DishService

    private val dish = Dish(
        id = 0,
        name = "Pizza",
        description = "Test dish",
        price = BigDecimal("499.0"),
        isAvailable = true,
        restaurantId = 1
    )

    @Test
    fun `create сохраняет блюдо через репозиторий`() {
        val saved = dish.copy(id = 1)
        `when`(dishRepositoryPort.create(dish)).thenReturn(saved)

        assertEquals(saved, dishService.create(dish))
    }

    @Test
    fun `findByIds возвращает список блюд`() {
        val dishes = listOf(dish.copy(id = 1), dish.copy(id = 2, name = "Pasta"))
        `when`(dishRepositoryPort.findByIds(listOf(1, 2))).thenReturn(dishes)

        assertEquals(dishes, dishService.findByIds(listOf(1, 2)))
    }

    @Test
    fun `findByRestaurantId возвращает меню ресторана`() {
        val menu = listOf(dish.copy(id = 1))
        `when`(dishRepositoryPort.findByRestaurantId(1)).thenReturn(menu)

        assertEquals(menu, dishService.findByRestaurantId(1))
    }

    @Test
    fun `deleteById делегирует удаление в репозиторий`() {
        `when`(dishRepositoryPort.deleteById(1)).thenReturn(true)

        assertEquals(true, dishService.deleteById(1))
    }
}
