package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.Withdrawal
import com.argbot.domain.port.input.WithdrawUsdcCommand
import com.argbot.domain.port.input.WithdrawUsdcUseCase
import com.argbot.domain.port.output.CapitalPort

@UseCase
class WithdrawUsdcService(
    private val capitalPort: CapitalPort
) : WithdrawUsdcUseCase {

    override fun execute(command: WithdrawUsdcCommand): Withdrawal {
        return capitalPort.submitWithdrawal(command.apiKey, command.apiSecret, command.address, command.amountUsdc)
    }
}
