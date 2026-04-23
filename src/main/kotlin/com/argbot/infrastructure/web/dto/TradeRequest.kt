package com.argbot.infrastructure.web.dto

import java.math.BigDecimal

data class TradeRequest(
    val encKey: String,
    val encSecret: String,
    val amountEur: BigDecimal
)
