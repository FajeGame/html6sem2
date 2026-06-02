package com.example.lab3.api

import com.example.lab3.support.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class RestaurantCacheTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun clearCache() {
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `повторный GET restaurants не перезаписывает кэш`() {
        mockMvc.get("/api/v1/restaurants").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))

        mockMvc.get("/api/v1/restaurants").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `создание ресторана инвалидирует кэш restaurants`() {
        mockMvc.get("/api/v1/restaurants").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))

        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Cache Test Place", "address": "ул. Кэша, 1"}"""
        }.andExpect { status { isCreated() } }

        assertNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `update ресторана обновляет запись в кэше`() {
        val createResponse = mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Before Update", "address": "ул. Старая, 1"}"""
        }.andReturn()

        val id = Regex(""""id"\s*:\s*(\d+)""")
            .find(createResponse.response.contentAsString)
            ?.groupValues
            ?.get(1)
            ?.toLong()
            ?: error("Не удалось извлечь id")

        mockMvc.get("/api/v1/restaurants/$id").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("restaurants")?.get(id))

        mockMvc.put("/api/v1/restaurants/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "After Update", "address": "ул. Новая, 2"}"""
        }.andExpect { status { isOk() } }

        assertNotNull(cacheManager.getCache("restaurants")?.get(id))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `удаление блюда инвалидирует кэш dishes и restaurants`() {
        val unique = UUID.randomUUID().toString()
        val restaurantResponse = mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Dish Cache $unique", "address": "ул. Меню, 1"}"""
        }.andExpect { status { isCreated() } }.andReturn()

        val restaurantId = Regex(""""id"\s*:\s*(\d+)""")
            .find(restaurantResponse.response.contentAsString)
            ?.groupValues
            ?.get(1)
            ?.toLong()
            ?: error("Не удалось извлечь restaurantId")

        mockMvc.get("/api/v1/restaurants/$restaurantId/dishes").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("dishes")?.get(restaurantId))

        mockMvc.get("/api/v1/restaurants").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))

        val dishResponse = mockMvc.post("/api/v1/restaurants/$restaurantId/dishes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Soup $unique", "description": "Test", "price": 250.00, "isAvailable": true}"""
        }.andExpect { status { isCreated() } }.andReturn()

        val dishId = Regex(""""id"\s*:\s*(\d+)""")
            .find(dishResponse.response.contentAsString)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь dishId")

        mockMvc.get("/api/v1/restaurants/$restaurantId/dishes").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("dishes")?.get(restaurantId))

        mockMvc.get("/api/v1/restaurants").andExpect { status { isOk() } }
        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))

        mockMvc.delete("/api/v1/dishes/$dishId").andExpect { status { isNoContent() } }

        assertNull(cacheManager.getCache("dishes")?.get(restaurantId))
        assertNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
    }
}
