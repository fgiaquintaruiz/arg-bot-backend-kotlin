package com.argbot.domain.port.output

import com.argbot.domain.model.ExchangeBalance

// Port OUT — operaciones de spot trading. Disponibles en testnet Y producción.
// Mapea a endpoints /api de Binance.
interface SpotTradingPort {
    fun getBalances(apiKey: String, apiSecret: String): ExchangeBalance
}
