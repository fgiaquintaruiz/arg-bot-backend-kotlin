package com.argbot.domain.model

import java.math.BigDecimal

data class ExchangeRate(val eurUsdt: BigDecimal) {
    companion object {
        fun default() = ExchangeRate(BigDecimal("1.08"))
    }
}
