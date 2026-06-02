package com.example.lab3.api

import com.example.lab3.domain.Restaurant
import jakarta.validation.constraints.NotBlank

data class RestaurantRequest(
    @field:NotBlank(message = "name is required")
    val name: String? = null,
    @field:NotBlank(message = "address is required")
    val address: String? = null
)

data class RestaurantResponse(
    val id: Long,
    val name: String,
    val address: String
)

fun RestaurantRequest.toDomain(id: Long = 0): Restaurant = Restaurant(
    id = id,
    name = name ?: "",
    address = address ?: ""
)

fun Restaurant.toResponse(): RestaurantResponse = RestaurantResponse(
    id = id,
    name = name,
    address = address
)
