package com.argbot.domain.model

import java.math.BigDecimal

// Aggregate — agrega todos los datos de mercado que necesita el frontend en una sola consulta
data class MarketData(
    val balances: ExchangeBalance,
    val exchangeRate: ExchangeRate,
    val p2pRate: P2PRate,             // Binance P2P (Criptoya)
    val ripioRate: P2PRate,           // Ripio
    val nexoRate: P2PRate,            // Nexo (via Criptoya)
    val withdrawalFee: WithdrawalFee,
    val tradingFeeRate: BigDecimal = BigDecimal("0.001")
)
