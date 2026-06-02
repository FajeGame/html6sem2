package com.example.lab3.application

import com.example.lab3.domain.Dish
import com.example.lab3.domain.DishRepositoryPort
import org.springframework.stereotype.Service

@Service
class DishService(
    private val dishRepositoryPort: DishRepositoryPort
) {
    fun create(dish: Dish): Dish = dishRepositoryPort.create(dish)

    fun update(id: Long, dish: Dish): Dish? = dishRepositoryPort.update(dish.copy(id = id))

    fun findById(id: Long): Dish? = dishRepositoryPort.findById(id)

    fun findByIds(ids: List<Long>): List<Dish> = dishRepositoryPort.findByIds(ids)

    fun findByRestaurantId(restaurantId: Long): List<Dish> = dishRepositoryPort.findByRestaurantId(restaurantId)

    fun findAll(namePart: String?): List<Dish> = dishRepositoryPort.findAll(namePart)

    fun deleteById(id: Long): Boolean = dishRepositoryPort.deleteById(id)
}
