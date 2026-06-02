package com.example.lab3.application

import com.example.lab3.api.AlreadyExistsException
import com.example.lab3.domain.Restaurant
import com.example.lab3.domain.RestaurantRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RestaurantServiceTest {

    @Mock
    lateinit var restaurantRepositoryPort: RestaurantRepositoryPort

    @InjectMocks
    lateinit var restaurantService: RestaurantService

    @Test
    fun `create сохраняет ресторан если имя свободно`() {
        val restaurant = Restaurant(id = 0, name = "Pizza Place", address = "ул. Ленина, 1")
        val saved = restaurant.copy(id = 1)
        `when`(restaurantRepositoryPort.findByName("Pizza Place")).thenReturn(null)
        `when`(restaurantRepositoryPort.create(restaurant)).thenReturn(saved)

        val result = restaurantService.create(restaurant)

        assertEquals(saved, result)
        verify(restaurantRepositoryPort).create(restaurant)
    }

    @Test
    fun `create бросает AlreadyExistsException при дублировании имени`() {
        val restaurant = Restaurant(id = 0, name = "Pizza Place", address = "ул. Мира, 5")
        `when`(restaurantRepositoryPort.findByName("Pizza Place"))
            .thenReturn(Restaurant(id = 2, name = "Pizza Place", address = "другой адрес"))

        assertThrows<AlreadyExistsException> {
            restaurantService.create(restaurant)
        }

        verify(restaurantRepositoryPort, never()).create(restaurant)
    }

    @Test
    fun `findById возвращает ресторан если он существует`() {
        val restaurant = Restaurant(id = 1, name = "Pizza Place", address = "ул. Ленина, 1")
        `when`(restaurantRepositoryPort.findById(1)).thenReturn(restaurant)

        assertEquals(restaurant, restaurantService.findById(1))
    }

    @Test
    fun `findById возвращает null для несуществующего id`() {
        `when`(restaurantRepositoryPort.findById(999)).thenReturn(null)

        assertNull(restaurantService.findById(999))
    }

    @Test
    fun `deleteById возвращает true если ресторан удалён`() {
        `when`(restaurantRepositoryPort.deleteById(1)).thenReturn(true)

        assertTrue(restaurantService.deleteById(1))
    }

    @Test
    fun `deleteById возвращает false если ресторан не найден`() {
        `when`(restaurantRepositoryPort.deleteById(999)).thenReturn(false)

        assertFalse(restaurantService.deleteById(999))
    }
}
