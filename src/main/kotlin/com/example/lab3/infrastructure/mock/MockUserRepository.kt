package com.example.lab3.infrastructure.mock

import com.example.lab3.domain.User
import com.example.lab3.domain.UserRepositoryPort
import java.util.concurrent.ConcurrentHashMap

class MockUserRepository : UserRepositoryPort {
    private val storage = ConcurrentHashMap<Long, User>()
    private var seq = 1L

    override fun create(user: User): User {
        val saved = user.copy(id = seq++)
        storage[saved.id] = saved
        return saved
    }

    override fun update(user: User): User? {
        if (!storage.containsKey(user.id)) return null
        storage[user.id] = user
        return user
    }

    override fun findById(id: Long): User? = storage[id]

    override fun findByEmail(email: String): User? = storage.values.firstOrNull { it.email == email }

    override fun existsByEmail(email: String): Boolean = storage.values.any { it.email == email }

    override fun findAll(): List<User> = storage.values.sortedBy { it.id }

    override fun deleteById(id: Long): Boolean = storage.remove(id) != null
}
