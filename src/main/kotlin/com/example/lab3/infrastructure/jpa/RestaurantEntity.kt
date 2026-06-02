package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Restaurant
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "restaurants")
class RestaurantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false)
    var address: String = "",
    @OneToMany(mappedBy = "restaurant")
    var dishes: MutableList<DishEntity> = mutableListOf()
) {
    fun toDomain(): Restaurant = Restaurant(
        id = id,
        name = name,
        address = address
    )

    companion object {
        fun fromDomain(restaurant: Restaurant): RestaurantEntity = RestaurantEntity(
            id = restaurant.id,
            name = restaurant.name,
            address = restaurant.address
        )
    }
}
