package com.argbot.domain.model

import java.math.BigDecimal

// Aggregate — agrega todos los datos de mercado que necesita el frontend en una sola consulta
data class MarketData(
    val balances: ExchangeBalance,
    val exchangeRate: ExchangeRate,
    val p2pRate: P2PRate,
    val ripioRate: P2PRate,
    val nexoRate: P2PRate,
    val withdrawalFee: WithdrawalFee,
    val tradingFeeRate: BigDecimal = BigDecimal("0.001"),
    val testnet: Boolean = true
)
