package com.argbot.infrastructure.web.dto

// Builder pattern implícito — Jackson usa los campos para construir el objeto desde JSON
data class MarketDataRequest(
    val encKey: String? = null,
    val encSecret: String? = null,
    val testnet: Boolean = true
)
