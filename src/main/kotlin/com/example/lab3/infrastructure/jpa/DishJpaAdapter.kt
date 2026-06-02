package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Dish
import com.example.lab3.domain.DishRepositoryPort

class DishJpaAdapter(
    private val dishJpaRepository: DishJpaRepository,
    private val restaurantJpaRepository: RestaurantJpaRepository
) : DishRepositoryPort {
    override fun create(dish: Dish): Dish {
        val restaurant = restaurantJpaRepository.findById(dish.restaurantId).orElseThrow()
        return dishJpaRepository.save(DishEntity.fromDomain(dish, restaurant)).toDomain()
    }

    override fun update(dish: Dish): Dish? {
        if (!dishJpaRepository.existsById(dish.id)) return null
        val restaurant = restaurantJpaRepository.findById(dish.restaurantId).orElseThrow()
        return dishJpaRepository.save(DishEntity.fromDomain(dish, restaurant)).toDomain()
    }

    override fun findById(id: Long): Dish? = dishJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByIds(ids: List<Long>): List<Dish> = dishJpaRepository.findAllByIdIn(ids).map { it.toDomain() }

    override fun findByRestaurantId(restaurantId: Long): List<Dish> =
        dishJpaRepository.findAllByRestaurantIdOrderById(restaurantId).map { it.toDomain() }

    override fun findAll(namePart: String?): List<Dish> {
        val entities = if (namePart.isNullOrBlank()) {
            dishJpaRepository.findAll()
        } else {
            dishJpaRepository.findByNamePart(namePart)
        }
        return entities.map { it.toDomain() }
    }

    override fun deleteById(id: Long): Boolean {
        if (!dishJpaRepository.existsById(id)) return false
        dishJpaRepository.deleteById(id)
        return true
    }
}
