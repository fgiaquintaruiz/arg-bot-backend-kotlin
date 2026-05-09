package com.argbot.infrastructure.binance

import com.argbot.infrastructure.exception.ExternalProviderException

class BinanceApiException(
    technicalMessage: String,
    userMessage: String = technicalMessage,
    cause: Throwable? = null
) : ExternalProviderException(
    message = technicalMessage,
    code = "BINANCE_ERROR",
    userMessage = userMessage,
    cause = cause
)
