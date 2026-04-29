package com.argbot.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonAlias
import java.math.BigDecimal

data class WithdrawRequest(
    @JsonAlias("apiKey")    val encKey: String,
    @JsonAlias("apiSecret") val encSecret: String,
    val address: String,
    val amountUsdc: BigDecimal
)
