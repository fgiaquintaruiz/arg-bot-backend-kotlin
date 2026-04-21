package com.argbot.domain.model

import java.math.BigDecimal

data class WithdrawalFee(val coin: String, val network: String, val amount: BigDecimal) {
    companion object {
        fun default() = WithdrawalFee("USDC", "BSC", BigDecimal("0.80"))
    }
}
