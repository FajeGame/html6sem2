package com.example.lab3.api

import com.example.lab3.domain.User
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class UserRequest(
    @field:NotBlank(message = "email is required")
    val email: String? = null,
    @field:NotBlank(message = "firstName is required")
    val firstName: String? = null,
    @field:NotBlank(message = "lastName is required")
    val lastName: String? = null,
    @JsonProperty("isActive")
    val isActive: Boolean? = true
)

data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    @JsonProperty("isActive")
    val isActive: Boolean
)

fun UserRequest.toDomain(id: Long = 0): User = User(
    id = id,
    email = email ?: "",
    firstName = firstName ?: "",
    lastName = lastName ?: "",
    isActive = isActive ?: true
)

fun User.toResponse(): UserResponse = UserResponse(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    isActive = isActive
)
