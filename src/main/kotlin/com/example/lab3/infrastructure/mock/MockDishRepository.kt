package com.example.lab3.infrastructure.mock

import com.example.lab3.domain.Dish
import com.example.lab3.domain.DishRepositoryPort
import java.util.concurrent.ConcurrentHashMap

class MockDishRepository : DishRepositoryPort {
    private val storage = ConcurrentHashMap<Long, Dish>()
    private var seq = 1L

    override fun create(dish: Dish): Dish {
        val saved = dish.copy(id = seq++)
        storage[saved.id] = saved
        return saved
    }

    override fun update(dish: Dish): Dish? {
        if (!storage.containsKey(dish.id)) return null
        storage[dish.id] = dish
        return dish
    }

    override fun findById(id: Long): Dish? = storage[id]

    override fun findByIds(ids: List<Long>): List<Dish> = ids.mapNotNull { storage[it] }

    override fun findByRestaurantId(restaurantId: Long): List<Dish> =
        storage.values.filter { it.restaurantId == restaurantId }.sortedBy { it.id }

    override fun findAll(namePart: String?): List<Dish> {
        val filtered = if (namePart.isNullOrBlank()) {
            storage.values
        } else {
            storage.values.filter { it.name.contains(namePart, ignoreCase = true) }
        }
        return filtered.sortedBy { it.id }
    }

    override fun deleteById(id: Long): Boolean = storage.remove(id) != null
}
