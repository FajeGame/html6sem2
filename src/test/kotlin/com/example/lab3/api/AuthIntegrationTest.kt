package com.example.lab3.api

import com.example.lab3.support.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

@Transactional
class AuthIntegrationTest : AbstractIntegrationTest() {

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
    fun `login возвращает 200 и JWT`() {
        val email = "login-${UUID.randomUUID()}@test.com"
        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"password123","name":"Ivan"}"""
        }.andExpect { status { isCreated() } }

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"password123"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { exists() }
            jsonPath("$.email") { value(email) }
        }
    }

    @Test
    fun `register с занятым email возвращает 409`() {
        val email = "dup-${UUID.randomUUID()}@test.com"
        val payload = """{"email":"$email","password":"password123","name":"Ivan"}"""
        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect { status { isCreated() } }

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect {
            status { isConflict() }
            jsonPath("$.status") { value(409) }
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
            content = """{"email":"$email","password":"wrongpassword"}"""
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.status") { value(401) }
        }
    }

    @Test
    fun `POST restaurant без токена возвращает 401`() {
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "No Auth", "address": "ул. Тест, 1"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `POST restaurant от USER возвращает 403`() {
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "User Place", "address": "ул. Тест, 1"}"""
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant от ADMIN возвращает 201`() {
        val name = "Admin Place ${UUID.randomUUID()}"
        mockMvc.post("/api/v1/restaurants") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "$name", "address": "ул. Админ, 1"}"""
        }.andExpect {
            status { isCreated() }
        }
    }
}
