package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Restaurant
import com.example.lab3.domain.RestaurantRepositoryPort

class RestaurantJpaAdapter(
    private val restaurantJpaRepository: RestaurantJpaRepository
) : RestaurantRepositoryPort {
    override fun create(restaurant: Restaurant): Restaurant =
        restaurantJpaRepository.save(RestaurantEntity.fromDomain(restaurant)).toDomain()

    override fun update(restaurant: Restaurant): Restaurant? {
        if (!restaurantJpaRepository.existsById(restaurant.id)) return null
        return restaurantJpaRepository.save(RestaurantEntity.fromDomain(restaurant)).toDomain()
    }

    override fun findById(id: Long): Restaurant? = restaurantJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByName(name: String): Restaurant? = restaurantJpaRepository.findFirstByNameIgnoreCase(name)?.toDomain()

    override fun findAll(): List<Restaurant> = restaurantJpaRepository.findAll().map { it.toDomain() }

    override fun deleteById(id: Long): Boolean {
        if (!restaurantJpaRepository.existsById(id)) return false
        restaurantJpaRepository.deleteById(id)
        return true
    }
}
