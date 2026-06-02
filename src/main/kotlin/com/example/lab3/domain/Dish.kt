package com.example.lab3.domain

import java.math.BigDecimal

data class Dish(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
    val restaurantId: Long
)
