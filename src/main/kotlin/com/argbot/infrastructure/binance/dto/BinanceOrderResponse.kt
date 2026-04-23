package com.argbot.infrastructure.binance.dto

data class BinanceOrderResponse(
    val orderId: Long,
    val symbol: String,
    val status: String,
    val executedQty: String,
    val cummulativeQuoteQty: String  // typo intencional — así lo devuelve Binance
)
