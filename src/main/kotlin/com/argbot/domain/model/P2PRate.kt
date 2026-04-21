package com.argbot.domain.model

import java.math.BigDecimal

data class P2PRate(val usdcArs: BigDecimal) {
    companion object {
        fun default() = P2PRate(BigDecimal("1150.50"))
    }
}
