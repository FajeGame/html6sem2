package com.example.lab3.application

import com.example.lab3.api.AlreadyExistsException
import com.example.lab3.api.AuthResponse
import com.example.lab3.api.LoginRequest
import com.example.lab3.api.NotFoundException
import com.example.lab3.api.RegisterRequest
import com.example.lab3.domain.Role
import com.example.lab3.infrastructure.jpa.UserEntity
import com.example.lab3.infrastructure.jpa.UserJpaRepository
import com.example.lab3.security.JwtService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userJpaRepository: UserJpaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    private val logger = KotlinLogging.logger {}

    fun register(request: RegisterRequest): AuthResponse {
        if (userJpaRepository.existsByEmail(request.email)) {
            throw AlreadyExistsException("User with email '${request.email}' already exists")
        }

        val savedUser = userJpaRepository.save(
            UserEntity(
                email = request.email,
                firstName = request.name,
                lastName = "",
                isActive = true,
                password = passwordEncoder.encode(request.password),
                role = Role.USER
            )
        )

        logger.info { "Registered user: ${savedUser.email}" }

        val token = jwtService.generateToken(savedUser.email, savedUser.role.name)
        return AuthResponse(token, savedUser.email, savedUser.role.name)
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userJpaRepository.findByEmail(request.email)
            ?: throw NotFoundException("User not found")

        logger.info { "User logged in: ${user.email}" }

        val token = jwtService.generateToken(user.email, user.role.name)
        return AuthResponse(token, user.email, user.role.name)
    }
}
