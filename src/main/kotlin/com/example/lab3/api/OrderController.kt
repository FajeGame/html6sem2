package com.example.lab3.api

import com.example.lab3.application.OrderService
import com.example.lab3.domain.OrderStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Управление заказами")
class OrderController(
    private val orderService: OrderService,
    private val userRepositoryPort: com.example.lab3.domain.UserRepositoryPort
) {
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Создать заказ")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Заказ создан"),
            ApiResponse(responseCode = "401", description = "Требуется JWT")
        ]
    )
    fun create(
        @Valid @RequestBody body: OrderCreateRequest,
        @org.springframework.security.core.annotation.AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<OrderResponse> {
        val user = userRepositoryPort.findByEmail(userDetails.username)
            ?: throw NotFoundException("User not found")
        val order = orderService.create(
            userId = user.id,
            dishIds = body.dishIds ?: throw BadRequestException("dishIds must not be empty")
        )
        val dishes = orderService.getDishes(order)
        return ResponseEntity.status(HttpStatus.CREATED).body(order.toResponse(dishes))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun findById(
        @PathVariable id: Long,
        @org.springframework.security.core.annotation.AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<OrderResponse> {
        val isAdmin = userDetails.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))
        val order = orderService.getOrderForUser(id, userDetails.username, isAdmin)
        val dishes = orderService.getDishes(order)
        return ResponseEntity.ok(order.toResponse(dishes))
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun findAll(
        @RequestParam(required = false) status: OrderStatus?,
        @org.springframework.security.core.annotation.AuthenticationPrincipal userDetails: UserDetails
    ): List<OrderResponse> {
        val isAdmin = userDetails.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))
        return orderService.findAllForUser(userDetails.username, isAdmin, status).map { order ->
            order.toResponse(orderService.getDishes(order))
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!orderService.deleteById(id)) throw NotFoundException("Order with id=$id not found")
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateStatus(@PathVariable id: Long, @Valid @RequestBody body: OrderStatusUpdateRequest): ResponseEntity<OrderResponse> {
        if (orderService.findById(id) == null) throw NotFoundException("Order with id=$id not found")
        val updated = orderService.updateStatus(id, body.status ?: throw BadRequestException("status is required"))
        val dishes = orderService.getDishes(updated)
        return ResponseEntity.ok(updated.toResponse(dishes))
    }
}
