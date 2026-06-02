package com.example.lab3.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.boot.testcontainers.service.connection.ServiceConnection

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class RestaurantIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("integration-tests-db")
            withUsername("test")
            withPassword("test")
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant возвращает 201 и создаёт запись`() {
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Integration Place", "address": "ул. Тестовая, 1"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.name") { value("Integration Place") }
            jsonPath("$.address") { value("ул. Тестовая, 1") }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `GET restaurant по id возвращает 200 и данные`() {
        val createResponse = mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Get Test Place", "address": "ул. Читаемая, 2"}"""
        }.andReturn()

        val id = Regex(""""id"\s*:\s*(\d+)""")
            .find(createResponse.response.contentAsString)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь id из ответа")

        mockMvc.get("/api/v1/restaurants/$id")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(id.toInt()) }
                jsonPath("$.name") { value("Get Test Place") }
                jsonPath("$.address") { value("ул. Читаемая, 2") }
            }
    }

    @Test
    fun `GET несуществующий ресторан возвращает 404`() {
        mockMvc.get("/api/v1/restaurants/999999")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { exists() }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant с пустым именем возвращает 400 и errors`() {
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "", "address": "ул. Тестовая, 1"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.errors.name") { exists() }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant с дублирующимся именем возвращает 409`() {
        val payload = """{"name": "Duplicate Place", "address": "ул. Первая, 1"}"""
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Duplicate Place", "address": "ул. Вторая, 2"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.status") { value(409) }
            jsonPath("$.message") { exists() }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `PUT несуществующий ресторан возвращает 404`() {
        mockMvc.put("/api/v1/restaurants/999999") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Ghost", "address": "ул. Нет, 0"}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.status") { value(404) }
        }
    }
}
