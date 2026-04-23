package com.argbot.domain.model

import java.math.BigDecimal

data class TradeOrder(
    val orderId: Long,
    val symbol: String,
    val status: String,
    val executedQty: BigDecimal,         // EUR vendidos
    val cumulativeQuoteQty: BigDecimal   // USDC recibidos
)
