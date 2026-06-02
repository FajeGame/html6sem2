package com.example.lab3.application

import com.example.lab3.domain.User
import com.example.lab3.domain.UserRepositoryPort
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepositoryPort: UserRepositoryPort
) {
    fun createOrGet(user: User): Pair<User, Boolean> {
        val existing = userRepositoryPort.findByEmail(user.email)
        if (existing != null) return existing to false
        return userRepositoryPort.create(user) to true
    }

    fun update(id: Long, user: User): User? = userRepositoryPort.update(user.copy(id = id))

    fun findById(id: Long): User? = userRepositoryPort.findById(id)

    fun findAll(): List<User> = userRepositoryPort.findAll()

    fun deleteById(id: Long): Boolean = userRepositoryPort.deleteById(id)
}
