package com.argbot.infrastructure.exception

sealed class ExternalProviderException(
    message: String,
    val code: String,
    val userMessage: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
