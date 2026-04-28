package com.argbot.application.usecase

import com.argbot.domain.model.TradeOrder
import com.argbot.domain.port.input.ExecuteTradeCommand
import com.argbot.domain.port.output.SpotTradingPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ExecuteTradeServiceTest {

    private val spotTradingPort = mockk<SpotTradingPort>()
    private val service = ExecuteTradeService(spotTradingPort)

    @Test
    fun `uses plain text apiKey and apiSecret directly without decryption`() {
        val command = ExecuteTradeCommand(
            apiKey    = "plain-api-key",
            apiSecret = "plain-api-secret",
            amountEur = BigDecimal("100.00")
        )
        val expectedOrder = TradeOrder(
            orderId              = 1L,
            symbol               = "EURUSDC",
            status               = "FILLED",
            executedQty          = BigDecimal("100.00"),
            cumulativeQuoteQty   = BigDecimal("109.00")
        )
        every {
            spotTradingPort.placeMarketOrder("plain-api-key", "plain-api-secret", "EURUSDC", "SELL", BigDecimal("100.00"))
        } returns expectedOrder

        val result = service.execute(command)

        assertThat(result).isEqualTo(expectedOrder)
        verify(exactly = 1) {
            spotTradingPort.placeMarketOrder("plain-api-key", "plain-api-secret", "EURUSDC", "SELL", BigDecimal("100.00"))
        }
    }
}
