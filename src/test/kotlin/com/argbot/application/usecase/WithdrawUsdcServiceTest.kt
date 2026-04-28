package com.argbot.application.usecase

import com.argbot.domain.model.Withdrawal
import com.argbot.domain.port.input.WithdrawUsdcCommand
import com.argbot.domain.port.output.CapitalPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class WithdrawUsdcServiceTest {

    private val capitalPort = mockk<CapitalPort>()
    private val service = WithdrawUsdcService(capitalPort)

    @Test
    fun `uses plain text apiKey and apiSecret directly without decryption`() {
        val command = WithdrawUsdcCommand(
            apiKey    = "plain-api-key",
            apiSecret = "plain-api-secret",
            address   = "0xABCDEF1234",
            amountUsdc = BigDecimal("50.00")
        )
        val expectedWithdrawal = Withdrawal(id = "withdraw-id-123")
        every {
            capitalPort.submitWithdrawal("plain-api-key", "plain-api-secret", "0xABCDEF1234", BigDecimal("50.00"))
        } returns expectedWithdrawal

        val result = service.execute(command)

        assertThat(result).isEqualTo(expectedWithdrawal)
        verify(exactly = 1) {
            capitalPort.submitWithdrawal("plain-api-key", "plain-api-secret", "0xABCDEF1234", BigDecimal("50.00"))
        }
    }
}
