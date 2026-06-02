package com.example.lab3.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DishJpaRepository : JpaRepository<DishEntity, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["restaurant"])
    fun findAllByIdIn(ids: List<Long>): List<DishEntity>

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["restaurant"])
    fun findAllByRestaurantIdOrderById(restaurantId: Long): List<DishEntity>

    @Query(
        """
        select d
        from DishEntity d
        where lower(d.name) like lower(concat('%', :namePart, '%'))
        order by d.id
        """
    )
    fun findByNamePart(namePart: String): List<DishEntity>
}
