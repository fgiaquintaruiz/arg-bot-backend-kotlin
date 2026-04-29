package com.argbot.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonAlias
import java.math.BigDecimal

data class TradeRequest(
    @JsonAlias("apiKey")    val encKey: String,
    @JsonAlias("apiSecret") val encSecret: String,
    val amountEur: BigDecimal
)
