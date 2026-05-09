package com.argbot.infrastructure.web

data class ErrorResponse(
    val error: String,
    val code: String,
    val retryAfter: Int? = null
)
