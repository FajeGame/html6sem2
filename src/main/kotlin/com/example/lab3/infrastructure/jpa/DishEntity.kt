package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Dish
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "dishes")
class DishEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false, length = 2000)
    var description: String = "",
    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false)
    var isAvailable: Boolean = true,
    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurant: RestaurantEntity = RestaurantEntity()
) {
    fun toDomain(): Dish = Dish(
        id = id,
        name = name,
        description = description,
        price = price,
        isAvailable = isAvailable,
        restaurantId = restaurant.id
    )

    companion object {
        fun fromDomain(dish: Dish, restaurant: RestaurantEntity): DishEntity = DishEntity(
            id = dish.id,
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
            restaurant = restaurant
        )
    }
}
