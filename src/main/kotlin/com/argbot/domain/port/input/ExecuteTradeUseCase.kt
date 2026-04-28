package com.argbot.domain.port.input

import com.argbot.domain.model.TradeOrder
import java.math.BigDecimal

interface ExecuteTradeUseCase {
    fun execute(command: ExecuteTradeCommand): TradeOrder
}

data class ExecuteTradeCommand(
    val apiKey: String,
    val apiSecret: String,
    val amountEur: BigDecimal
)
