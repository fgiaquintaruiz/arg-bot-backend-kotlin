package com.argbot.infrastructure.ip

import com.argbot.infrastructure.exception.ExternalProviderException

class IpifyApiException(
    message: String,
    cause: Throwable? = null
) : ExternalProviderException(
    message = message,
    code = "IPIFY_ERROR",
    userMessage = "No se pudo obtener la IP pública.",
    cause = cause
)
