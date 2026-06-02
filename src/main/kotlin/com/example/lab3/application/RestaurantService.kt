package com.example.lab3.application

import com.example.lab3.api.AlreadyExistsException
import com.example.lab3.domain.Restaurant
import com.example.lab3.domain.RestaurantRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RestaurantService(
    private val restaurantRepositoryPort: RestaurantRepositoryPort
) {
    private val logger = KotlinLogging.logger {}

    fun create(restaurant: Restaurant): Restaurant {
        ensureNameAvailable(restaurant.name)
        logger.info { "Creating restaurant name=${restaurant.name}" }
        return restaurantRepositoryPort.create(restaurant)
    }

    fun update(id: Long, restaurant: Restaurant): Restaurant? {
        ensureNameAvailable(restaurant.name, id)
        logger.info { "Updating restaurant id=$id" }
        return restaurantRepositoryPort.update(restaurant.copy(id = id))
    }

    fun findById(id: Long): Restaurant? = restaurantRepositoryPort.findById(id)

    fun findByName(name: String): Restaurant? = restaurantRepositoryPort.findByName(name)

    fun findAll(): List<Restaurant> = restaurantRepositoryPort.findAll()

    fun deleteById(id: Long): Boolean {
        val deleted = restaurantRepositoryPort.deleteById(id)
        if (deleted) {
            logger.info { "Deleted restaurant id=$id" }
        }
        return deleted
    }

    private fun ensureNameAvailable(name: String, currentId: Long? = null) {
        val existing = restaurantRepositoryPort.findByName(name) ?: return
        if (existing.id != currentId) {
            throw AlreadyExistsException("Restaurant with name='$name' already exists")
        }
    }
}
