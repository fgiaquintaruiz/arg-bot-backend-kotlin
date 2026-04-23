package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.Withdrawal
import com.argbot.domain.port.input.WithdrawUsdcCommand
import com.argbot.domain.port.input.WithdrawUsdcUseCase
import com.argbot.domain.port.output.CapitalPort
import com.argbot.domain.port.output.CryptoPort

@UseCase
class WithdrawUsdcService(
    private val capitalPort: CapitalPort,
    private val cryptoPort: CryptoPort
) : WithdrawUsdcUseCase {

    override fun execute(command: WithdrawUsdcCommand): Withdrawal {
        val apiKey    = cryptoPort.decrypt(command.encryptedApiKey)    ?: error("API key inválida")
        val apiSecret = cryptoPort.decrypt(command.encryptedApiSecret) ?: error("API secret inválido")
        return capitalPort.submitWithdrawal(apiKey, apiSecret, command.address, command.amountUsdc)
    }
}
