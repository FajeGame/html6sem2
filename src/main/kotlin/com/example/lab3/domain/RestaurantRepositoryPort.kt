package com.example.lab3.domain

interface RestaurantRepositoryPort {
    fun create(restaurant: Restaurant): Restaurant
    fun update(restaurant: Restaurant): Restaurant?
    fun findById(id: Long): Restaurant?
    fun findByName(name: String): Restaurant?
    fun findAll(): List<Restaurant>
    fun deleteById(id: Long): Boolean
}
