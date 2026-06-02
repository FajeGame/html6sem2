package com.example.lab3.domain

data class User(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean,
    val role: Role = Role.USER,
    val passwordHash: String? = null
)
