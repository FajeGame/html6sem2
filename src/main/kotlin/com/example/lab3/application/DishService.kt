package com.example.lab3.application

import com.example.lab3.domain.Dish
import com.example.lab3.domain.DishRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class DishService(
    private val dishRepositoryPort: DishRepositoryPort
) {
    private val logger = KotlinLogging.logger {}

    @Lazy
    @Autowired
    private lateinit var self: DishService

    @CacheEvict(cacheNames = ["dishes"], key = "#result.restaurantId")
    fun create(dish: Dish): Dish = dishRepositoryPort.create(dish)

    @CacheEvict(cacheNames = ["dishes"], key = "#result.restaurantId", condition = "#result != null")
    fun update(id: Long, dish: Dish): Dish? = dishRepositoryPort.update(dish.copy(id = id))

    fun findById(id: Long): Dish? = dishRepositoryPort.findById(id)

    fun findByIds(ids: List<Long>): List<Dish> = dishRepositoryPort.findByIds(ids)

    @Cacheable(cacheNames = ["dishes"], key = "#restaurantId")
    fun findByRestaurantId(restaurantId: Long): List<Dish> {
        logger.info { "Loading dishes for restaurantId=$restaurantId from DB" }
        return dishRepositoryPort.findByRestaurantId(restaurantId)
    }

    fun findAll(namePart: String?): List<Dish> = dishRepositoryPort.findAll(namePart)

    fun deleteById(id: Long): Boolean {
        val existing = dishRepositoryPort.findById(id) ?: return false
        return self.deleteByIdWithEvict(id, existing.restaurantId)
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = ["dishes"], key = "#restaurantId"),
            CacheEvict(cacheNames = ["restaurants"], allEntries = true)
        ]
    )
    fun deleteByIdWithEvict(id: Long, restaurantId: Long): Boolean =
        dishRepositoryPort.deleteById(id)
}
