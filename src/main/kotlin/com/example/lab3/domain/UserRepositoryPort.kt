package com.example.lab3.domain

interface UserRepositoryPort {
    fun create(user: User): User
    fun update(user: User): User?
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findAll(): List<User>
    fun deleteById(id: Long): Boolean
}
