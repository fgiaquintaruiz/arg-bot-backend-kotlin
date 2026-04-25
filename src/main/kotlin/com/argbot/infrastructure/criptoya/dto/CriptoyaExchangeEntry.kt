package com.argbot.infrastructure.criptoya.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CriptoyaExchangeEntry(
    val totalAsk: Double,
    val totalBid: Double,
    val time: Long
)
