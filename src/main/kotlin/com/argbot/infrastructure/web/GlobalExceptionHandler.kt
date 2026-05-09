package com.argbot.infrastructure.web

import com.argbot.infrastructure.exception.ExternalProviderException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CallNotPermittedException::class)
    fun handleCircuitBreaker(e: CallNotPermittedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header(HttpHeaders.RETRY_AFTER, "30")
            .body(
                ErrorResponse(
                    error = "Servicio temporalmente no disponible. Reintentá en unos segundos.",
                    code = "CIRCUIT_OPEN",
                    retryAfter = 30
                )
            )

    @ExceptionHandler(ExternalProviderException::class)
    fun handleExternalProvider(e: ExternalProviderException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(
                ErrorResponse(
                    error = e.userMessage,
                    code = e.code
                )
            )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = e.message ?: "Solicitud inválida.",
                    code = "BAD_REQUEST"
                )
            )

    @ExceptionHandler(Exception::class)
    fun handleUnknown(e: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    error = "Error interno del servidor.",
                    code = "INTERNAL_ERROR"
                )
            )
}
