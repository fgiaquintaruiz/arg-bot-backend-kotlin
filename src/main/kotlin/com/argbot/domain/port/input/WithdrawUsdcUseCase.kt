package com.argbot.domain.port.input

import com.argbot.domain.model.Withdrawal
import java.math.BigDecimal

interface WithdrawUsdcUseCase {
    fun execute(command: WithdrawUsdcCommand): Withdrawal
}

data class WithdrawUsdcCommand(
    val apiKey: String,
    val apiSecret: String,
    val address: String,
    val amountUsdc: BigDecimal
)
