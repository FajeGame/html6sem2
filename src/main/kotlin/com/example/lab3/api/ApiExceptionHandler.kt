package com.example.lab3.api

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

open class ErrorResponse(
    val status: Int,
    val message: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

class ValidationErrorResponse(
    status: Int,
    message: String? = null,
    val errors: Map<String, String>,
    timestamp: LocalDateTime = LocalDateTime.now()
) : ErrorResponse(status, message, timestamp)

sealed class AppException(message: String) : RuntimeException(message)

class NotFoundException(message: String) : AppException(message)

class AlreadyExistsException(message: String) : AppException(message)

class InvalidOrderStateException(message: String) : AppException(message)

class BadRequestException(message: String) : AppException(message)

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ResponseEntity<ErrorResponse> {
        val status = when (ex) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is AlreadyExistsException -> HttpStatus.CONFLICT
            is InvalidOrderStateException, is BadRequestException -> HttpStatus.BAD_REQUEST
        }

        if (status == HttpStatus.NOT_FOUND) {
            logger.warn { ex.message ?: status.reasonPhrase }
        }

        return ResponseEntity.status(status)
            .body(ErrorResponse(status.value(), ex.message))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"))

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), "Access denied"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ValidationErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Validation failed",
                    errors = errors
                )
            )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.constraintViolations.associate {
            it.propertyPath.toString().substringAfterLast('.') to it.message
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ValidationErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Validation failed",
                    errors = errors.ifEmpty { mapOf("request" to (ex.message ?: "Validation failed")) }
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleBadRequestBody(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request body"))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request parameter"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected server error" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"))
    }
}
