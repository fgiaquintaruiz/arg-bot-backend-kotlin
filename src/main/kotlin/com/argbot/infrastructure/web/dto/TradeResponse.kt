package com.argbot.infrastructure.web.dto

import com.argbot.domain.model.TradeOrder
import java.math.BigDecimal

data class TradeResponse(
    val success: Boolean,
    val orderId: Long,
    val status: String,
    val eurSold: BigDecimal,
    val usdcReceived: BigDecimal
) {
    companion object {
        fun from(order: TradeOrder) = TradeResponse(
            success      = true,
            orderId      = order.orderId,
            status       = order.status,
            eurSold      = order.executedQty,
            usdcReceived = order.cumulativeQuoteQty
        )
    }
}
