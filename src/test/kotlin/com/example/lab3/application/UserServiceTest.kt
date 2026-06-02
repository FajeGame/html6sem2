package com.example.lab3.application

import com.example.lab3.domain.User
import com.example.lab3.domain.UserRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    lateinit var userRepositoryPort: UserRepositoryPort

    @InjectMocks
    lateinit var userService: UserService

    private val user = User(id = 0, email = "test@example.com", firstName = "Ivan", lastName = "Petrov", isActive = true)

    @Test
    fun `createOrGet создаёт нового пользователя если email не занят`() {
        val saved = user.copy(id = 1)
        `when`(userRepositoryPort.findByEmail("test@example.com")).thenReturn(null)
        `when`(userRepositoryPort.create(user)).thenReturn(saved)

        val (result, created) = userService.createOrGet(user)

        assertEquals(saved, result)
        assertTrue(created)
    }

    @Test
    fun `createOrGet возвращает существующего пользователя без создания`() {
        val existing = user.copy(id = 5)
        `when`(userRepositoryPort.findByEmail("test@example.com")).thenReturn(existing)

        val (result, created) = userService.createOrGet(user)

        assertEquals(existing, result)
        assertFalse(created)
    }

    @Test
    fun `findById возвращает пользователя если он существует`() {
        val existing = user.copy(id = 1)
        `when`(userRepositoryPort.findById(1)).thenReturn(existing)

        assertEquals(existing, userService.findById(1))
    }

    @Test
    fun `deleteById делегирует удаление в репозиторий`() {
        `when`(userRepositoryPort.deleteById(1)).thenReturn(true)

        assertTrue(userService.deleteById(1))
    }
}
