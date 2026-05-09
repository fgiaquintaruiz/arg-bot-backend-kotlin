package com.argbot.infrastructure.adapter

import com.argbot.infrastructure.exception.ExternalProviderException

class CriptoyaApiException(
    message: String,
    cause: Throwable? = null
) : ExternalProviderException(
    message = message,
    code = "CRIPTOYA_ERROR",
    userMessage = "No hay datos disponibles del proveedor de tasas en este momento.",
    cause = cause
)
