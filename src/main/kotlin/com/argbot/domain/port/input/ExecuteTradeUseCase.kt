package com.argbot.domain.port.input

import com.argbot.domain.model.TradeOrder
import java.math.BigDecimal

interface ExecuteTradeUseCase {
    fun execute(command: ExecuteTradeCommand): TradeOrder
}

data class ExecuteTradeCommand(
    val encryptedApiKey: String,
    val encryptedApiSecret: String,
    val amountEur: BigDecimal
)
