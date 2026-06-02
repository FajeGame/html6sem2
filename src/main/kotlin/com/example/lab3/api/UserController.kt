package com.example.lab3.api

import com.example.lab3.application.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun create(@Valid @RequestBody body: UserRequest): ResponseEntity<UserResponse> {
        val (result, created) = userService.createOrGet(body.toDomain())
        val status = if (created) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(result.toResponse())
    }

    @GetMapping
    fun findAll(): List<UserResponse> = userService.findAll().map { it.toResponse() }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.findById(id) ?: throw NotFoundException("User with id=$id not found")
        return ResponseEntity.ok(user.toResponse())
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: UserRequest): ResponseEntity<UserResponse> {
        val user = userService.update(id, body.toDomain()) ?: throw NotFoundException("User with id=$id not found")
        return ResponseEntity.ok(user.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!userService.deleteById(id)) throw NotFoundException("User with id=$id not found")
        return ResponseEntity.noContent().build()
    }
}
