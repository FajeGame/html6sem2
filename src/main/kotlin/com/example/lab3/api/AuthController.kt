package com.example.lab3.api

import com.example.lab3.application.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Регистрация и аутентификация")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Пользователь создан, возвращён JWT"),
            ApiResponse(responseCode = "409", description = "Email уже занят")
        ]
    )
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    @Operation(summary = "Вход по email и паролю")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Успешный вход, возвращён JWT"),
            ApiResponse(responseCode = "401", description = "Неверный email или пароль")
        ]
    )
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }
}
