package com.example.lab3.infrastructure.mock

import com.example.lab3.domain.Restaurant
import com.example.lab3.domain.RestaurantRepositoryPort
import java.util.concurrent.ConcurrentHashMap

class MockRestaurantRepository : RestaurantRepositoryPort {
    private val storage = ConcurrentHashMap<Long, Restaurant>()
    private var seq = 1L

    override fun create(restaurant: Restaurant): Restaurant {
        val saved = restaurant.copy(id = seq++)
        storage[saved.id] = saved
        return saved
    }

    override fun update(restaurant: Restaurant): Restaurant? {
        if (!storage.containsKey(restaurant.id)) return null
        storage[restaurant.id] = restaurant
        return restaurant
    }

    override fun findById(id: Long): Restaurant? = storage[id]

    override fun findByName(name: String): Restaurant? = storage.values.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override fun findAll(): List<Restaurant> = storage.values.sortedBy { it.id }

    override fun deleteById(id: Long): Boolean = storage.remove(id) != null
}
