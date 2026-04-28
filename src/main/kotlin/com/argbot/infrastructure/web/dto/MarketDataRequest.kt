package com.argbot.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonAlias

// Builder pattern implícito — Jackson usa los campos para construir el objeto desde JSON.
// @JsonAlias acepta los nombres legacy que el frontend enviaba (apiKey/apiSecret)
// ademas del nombre canónico (encKey/encSecret).
data class MarketDataRequest(
    @JsonAlias("apiKey")    val encKey: String? = null,
    @JsonAlias("apiSecret") val encSecret: String? = null,
    val testnet: Boolean = true
)
