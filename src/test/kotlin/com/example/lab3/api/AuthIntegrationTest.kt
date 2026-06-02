package com.example.lab3.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

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
    fun `register возвращает 201 и JWT`() {
        val email = "user-${UUID.randomUUID()}@test.com"
        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"password123","name":"Ivan"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.token") { exists() }
            jsonPath("$.email") { value(email) }
            jsonPath("$.role") { value("USER") }
        }
    }

    @Test
    fun `register с дублирующимся email возвращает 409`() {
        val email = "dup-${UUID.randomUUID()}@test.com"
        val body = """{"email":"$email","password":"password123","name":"Ivan"}"""
        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isCreated() } }

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isConflict() }
            jsonPath("$.status") { value(409) }
        }
    }

    @Test
    fun `login с верными credentials возвращает 200 и JWT`() {
        val email = "login-${UUID.randomUUID()}@test.com"
        val password = "password123"
        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"$password","name":"Ivan"}"""
        }.andExpect { status { isCreated() } }

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"$password"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { exists() }
            jsonPath("$.email") { value(email) }
        }
    }

    @Test
    fun `login с неверным паролем возвращает 401`() {
        val email = "bad-${UUID.randomUUID()}@test.com"
        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"password123","name":"Ivan"}"""
        }.andExpect { status { isCreated() } }

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"wrong-password"}"""
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.status") { value(401) }
        }
    }

    @Test
    fun `POST restaurant без токена возвращает 401`() {
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Test", "address": "Street 1"}"""
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.status") { value(401) }
        }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `POST restaurant от USER возвращает 403`() {
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Test", "address": "Street 1"}"""
        }.andExpect {
            status { isForbidden() }
            jsonPath("$.status") { value(403) }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant от ADMIN возвращает 201`() {
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Admin Place ${UUID.randomUUID()}", "address": "Street 1"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
        }
    }
}
