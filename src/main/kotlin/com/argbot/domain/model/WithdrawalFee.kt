package com.argbot.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class WithdrawalFee(val coin: String, val network: String, val amount: BigDecimal) {
    companion object {
        val DEFAULT_AMOUNT: BigDecimal = BigDecimal("0.80").setScale(2, RoundingMode.HALF_UP)
        fun default() = WithdrawalFee("USDC", "BSC", DEFAULT_AMOUNT)
    }
}
