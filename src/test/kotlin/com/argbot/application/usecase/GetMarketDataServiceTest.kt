package com.argbot.application.usecase

import com.argbot.domain.model.*
import com.argbot.domain.port.input.GetMarketDataQuery
import com.argbot.domain.port.output.CapitalPort
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.domain.port.output.P2PRatePort
import com.argbot.domain.port.output.SpotTradingPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class GetMarketDataServiceTest {

    private val spotTradingPort  = mockk<SpotTradingPort>()
    private val capitalPort      = mockk<CapitalPort>()
    private val exchangeRatePort = mockk<ExchangeRatePort>()
    private val p2pRatePort      = mockk<P2PRatePort>()

    private val service = GetMarketDataService(spotTradingPort, capitalPort, exchangeRatePort, p2pRatePort)

    @Test
    fun `sin credenciales devuelve balances vacios y tasas de mercado`() {
        val query = GetMarketDataQuery(null, null)
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate(BigDecimal("1.09"))
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate(BigDecimal("1200.00"))
        every { p2pRatePort.getRipioUsdcArsRate() } returns P2PRate(BigDecimal("1200.00"))
        every { p2pRatePort.getNexoUsdcArsRate()  } returns P2PRate(BigDecimal("1100.00"))

        val result = service.execute(query)

        assertThat(result.balances).isEqualTo(ExchangeBalance.empty())
        assertThat(result.exchangeRate.eurUsdt).isEqualByComparingTo("1.09")
        assertThat(result.p2pRate.usdcArs).isEqualByComparingTo("1200.00")
        verify(exactly = 0) { spotTradingPort.getBalances(any(), any(), any()) }
        verify(exactly = 0) { capitalPort.getWithdrawalFee(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `con credenciales plain text obtiene balances de SpotTradingPort y fee de CapitalPort`() {
        val query = GetMarketDataQuery("real-key", "real-secret")
        every { spotTradingPort.getBalances("real-key", "real-secret", any()) } returns ExchangeBalance.of(100.0, 50.0)
        every { capitalPort.getWithdrawalFee("real-key", "real-secret", "USDC", "BSC", any()) } returns WithdrawalFee.default()
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate.default()
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate.default()
        every { p2pRatePort.getRipioUsdcArsRate() } returns P2PRate(BigDecimal("1200.00"))
        every { p2pRatePort.getNexoUsdcArsRate()  } returns P2PRate(BigDecimal("1100.00"))

        val result = service.execute(query)

        assertThat(result.balances.eur).isEqualByComparingTo("100.00")
        assertThat(result.balances.usdc).isEqualByComparingTo("50.00")
    }

    @Test
    fun `si SpotTradingPort falla devuelve balances vacios pero las tasas siguen`() {
        val query = GetMarketDataQuery("real-key", "real-secret")
        every { spotTradingPort.getBalances(any(), any(), any()) } throws RuntimeException("Binance timeout")
        every { capitalPort.getWithdrawalFee(any(), any(), any(), any(), any()) } throws RuntimeException()
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate(BigDecimal("1.09"))
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate(BigDecimal("1200.00"))
        every { p2pRatePort.getRipioUsdcArsRate() } returns P2PRate(BigDecimal("1200.00"))
        every { p2pRatePort.getNexoUsdcArsRate()  } returns P2PRate(BigDecimal("1100.00"))

        val result = service.execute(query)

        assertThat(result.balances).isEqualTo(ExchangeBalance.empty())
        assertThat(result.exchangeRate.eurUsdt).isEqualByComparingTo("1.09")
    }

    @Test
    fun `si CriptoYa falla usa tasa default`() {
        val query = GetMarketDataQuery(null, null)
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate.default()
        every { p2pRatePort.getUsdcArsRate() } throws RuntimeException("CriptoYa timeout")
        every { p2pRatePort.getRipioUsdcArsRate() } returns P2PRate(BigDecimal("1200.00"))
        every { p2pRatePort.getNexoUsdcArsRate()  } returns P2PRate(BigDecimal("1100.00"))

        val result = service.execute(query)

        assertThat(result.p2pRate).isEqualTo(P2PRate.default())
    }

    @Test
    fun `si Ripio falla usa tasa default`() {
        val query = GetMarketDataQuery(null, null)
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate.default()
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate.default()
        every { p2pRatePort.getRipioUsdcArsRate() } throws RuntimeException("Ripio timeout")
        every { p2pRatePort.getNexoUsdcArsRate()  } returns P2PRate(BigDecimal("1100.00"))

        val result = service.execute(query)

        assertThat(result.ripioRate).isEqualTo(P2PRate.default())
    }
}
