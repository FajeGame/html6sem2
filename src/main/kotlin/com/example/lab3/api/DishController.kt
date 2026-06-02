package com.example.lab3.api

import com.example.lab3.application.DishService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/dishes")
class DishController(
    private val dishService: DishService
) {
    @GetMapping
    fun findAll(@RequestParam(name = "namePart", required = false) namePart: String?): List<DishResponse> {
        return dishService.findAll(namePart).map { it.toResponse() }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<DishResponse> {
        val dish = dishService.findById(id) ?: throw NotFoundException("Dish with id=$id not found")
        return ResponseEntity.ok(dish.toResponse())
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: DishRequest): ResponseEntity<DishResponse> {
        val existing = dishService.findById(id) ?: throw NotFoundException("Dish with id=$id not found")
        val dish = dishService.update(id, body.toDomain(id = id, restaurantId = existing.restaurantId))
            ?: throw NotFoundException("Dish with id=$id not found")
        return ResponseEntity.ok(dish.toResponse())
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!dishService.deleteById(id)) throw NotFoundException("Dish with id=$id not found")
        return ResponseEntity.noContent().build()
    }
}
