package com.example.lab3.api

import com.example.lab3.domain.Dish
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class DishRequest(
    @field:NotBlank(message = "name is required")
    val name: String? = null,
    val description: String? = null,
    @field:NotNull(message = "price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
    val price: BigDecimal? = null,
    @JsonProperty("isAvailable")
    val isAvailable: Boolean? = true
)

data class DishResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    @JsonProperty("isAvailable")
    val isAvailable: Boolean,
    val restaurantId: Long
)

fun DishRequest.toDomain(id: Long = 0, restaurantId: Long): Dish = Dish(
    id = id,
    name = name ?: "",
    description = description ?: "",
    price = price ?: BigDecimal.ZERO,
    isAvailable = isAvailable ?: true,
    restaurantId = restaurantId
)

fun Dish.toResponse(): DishResponse = DishResponse(
    id = id,
    name = name,
    description = description,
    price = price,
    isAvailable = isAvailable,
    restaurantId = restaurantId
)
