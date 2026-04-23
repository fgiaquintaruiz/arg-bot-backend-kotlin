package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.TradeOrder
import com.argbot.domain.port.input.ExecuteTradeCommand
import com.argbot.domain.port.input.ExecuteTradeUseCase
import com.argbot.domain.port.output.CryptoPort
import com.argbot.domain.port.output.SpotTradingPort

@UseCase
class ExecuteTradeService(
    private val spotTradingPort: SpotTradingPort,
    private val cryptoPort: CryptoPort
) : ExecuteTradeUseCase {

    override fun execute(command: ExecuteTradeCommand): TradeOrder {
        val apiKey    = cryptoPort.decrypt(command.encryptedApiKey)    ?: error("API key inválida")
        val apiSecret = cryptoPort.decrypt(command.encryptedApiSecret) ?: error("API secret inválido")
        return spotTradingPort.placeMarketOrder(apiKey, apiSecret, "EURUSDC", "SELL", command.amountEur)
    }
}
