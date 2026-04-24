package com.argbot.infrastructure.ripio.dto

data class RipioTickerResponse(
    val data: List<RipioTickerData>
)

data class RipioTickerData(
    val pair: String,
    val bid: Double,
    val ask: Double
)
