package com.argbot.domain.port.output

import com.argbot.domain.model.ExchangeBalance
import com.argbot.domain.model.TradeOrder
import java.math.BigDecimal

// Port OUT — operaciones de spot trading. Disponibles en testnet Y producción.
// Mapea a endpoints /api de Binance.
interface SpotTradingPort {
    fun getBalances(apiKey: String, apiSecret: String, testnet: Boolean = true): ExchangeBalance
    fun placeMarketOrder(apiKey: String, apiSecret: String, symbol: String, side: String, quantity: BigDecimal): TradeOrder
}
