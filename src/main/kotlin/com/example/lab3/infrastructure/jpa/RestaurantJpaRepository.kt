package com.example.lab3.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantJpaRepository : JpaRepository<RestaurantEntity, Long> {
    fun findFirstByNameIgnoreCase(name: String): RestaurantEntity?
}
