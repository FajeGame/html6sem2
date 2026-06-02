package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.User
import com.example.lab3.domain.UserRepositoryPort

class UserJpaAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepositoryPort {
    override fun create(user: User): User {
        val entity = UserEntity.fromDomain(user)
        if (entity.password.isBlank()) {
            entity.password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        }
        return userJpaRepository.save(entity).toDomain()
    }

    override fun update(user: User): User? {
        if (!userJpaRepository.existsById(user.id)) return null
        return userJpaRepository.save(UserEntity.fromDomain(user)).toDomain()
    }

    override fun findById(id: Long): User? = userJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: String): User? = userJpaRepository.findByEmail(email)?.toDomain()

    override fun existsByEmail(email: String): Boolean = userJpaRepository.existsByEmail(email)

    override fun findAll(): List<User> = userJpaRepository.findAll().map { it.toDomain() }

    override fun deleteById(id: Long): Boolean {
        if (!userJpaRepository.existsById(id)) return false
        userJpaRepository.deleteById(id)
        return true
    }
}
