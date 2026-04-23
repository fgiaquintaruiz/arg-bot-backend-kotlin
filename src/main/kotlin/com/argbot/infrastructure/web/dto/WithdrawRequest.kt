package com.argbot.infrastructure.web.dto

import java.math.BigDecimal

data class WithdrawRequest(
    val encKey: String,
    val encSecret: String,
    val address: String,
    val amountUsdc: BigDecimal
)
