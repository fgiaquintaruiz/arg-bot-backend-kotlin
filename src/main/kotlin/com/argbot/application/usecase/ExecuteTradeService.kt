package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.TradeOrder
import com.argbot.domain.port.input.ExecuteTradeCommand
import com.argbot.domain.port.input.ExecuteTradeUseCase
import com.argbot.domain.port.output.SpotTradingPort

@UseCase
class ExecuteTradeService(
    private val spotTradingPort: SpotTradingPort
) : ExecuteTradeUseCase {

    override fun execute(command: ExecuteTradeCommand): TradeOrder {
        return spotTradingPort.placeMarketOrder(command.apiKey, command.apiSecret, "EURUSDC", "SELL", command.amountEur, command.testnet)
    }
}
