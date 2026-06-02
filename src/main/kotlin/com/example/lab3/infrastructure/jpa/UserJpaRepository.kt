package com.example.lab3.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun findAllByIsActiveTrue(): List<UserEntity>
}
