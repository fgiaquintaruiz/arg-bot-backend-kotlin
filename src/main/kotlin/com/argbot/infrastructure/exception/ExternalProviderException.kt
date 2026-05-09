package com.argbot.infrastructure.exception

abstract class ExternalProviderException(
    message: String,
    val code: String,
    val userMessage: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
