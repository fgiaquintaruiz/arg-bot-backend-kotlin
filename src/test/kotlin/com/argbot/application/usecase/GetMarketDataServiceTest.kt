package com.argbot.application.usecase

import com.argbot.domain.model.*
import com.argbot.domain.port.input.GetMarketDataQuery
import com.argbot.domain.port.output.BinancePort
import com.argbot.domain.port.output.CryptoPort
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.domain.port.output.P2PRatePort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

// TDD: estos tests se escriben ANTES que GetMarketDataService.
// Cada test es una especificación de comportamiento, no una prueba de implementación.
class GetMarketDataServiceTest {

    private val binancePort     = mockk<BinancePort>()
    private val exchangeRatePort = mockk<ExchangeRatePort>()
    private val p2pRatePort     = mockk<P2PRatePort>()
    private val cryptoPort      = mockk<CryptoPort>()

    private val service = GetMarketDataService(binancePort, exchangeRatePort, p2pRatePort, cryptoPort)

    @Test
    fun `sin credenciales devuelve balances vacios y tasas de mercado`() {
        val query = GetMarketDataQuery(null, null)
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate(BigDecimal("1.09"))
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate(BigDecimal("1200.00"))

        val result = service.execute(query)

        assertThat(result.balances).isEqualTo(BinanceBalance.empty())
        assertThat(result.exchangeRate.eurUsdt).isEqualByComparingTo("1.09")
        assertThat(result.p2pRate.usdcArs).isEqualByComparingTo("1200.00")
        verify(exactly = 0) { binancePort.getBalances(any(), any()) }
    }

    @Test
    fun `con credenciales validas obtiene balances y fee de Binance`() {
        val query = GetMarketDataQuery("enc-key", "enc-secret")
        every { cryptoPort.decrypt("enc-key")    } returns "real-key"
        every { cryptoPort.decrypt("enc-secret") } returns "real-secret"
        every { binancePort.getBalances("real-key", "real-secret") } returns
            BinanceBalance.of(100.0, 50.0)
        every { binancePort.getWithdrawalFee("real-key", "real-secret", "USDC", "BSC") } returns
            WithdrawalFee.default()
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate.default()
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate.default()

        val result = service.execute(query)

        assertThat(result.balances.eur).isEqualByComparingTo("100.00")
        assertThat(result.balances.usdc).isEqualByComparingTo("50.00")
    }

    @Test
    fun `si Binance falla devuelve balances vacios pero las tasas siguen`() {
        val query = GetMarketDataQuery("enc-key", "enc-secret")
        every { cryptoPort.decrypt("enc-key")    } returns "real-key"
        every { cryptoPort.decrypt("enc-secret") } returns "real-secret"
        every { binancePort.getBalances(any(), any()) } throws RuntimeException("Binance timeout")
        every { binancePort.getWithdrawalFee(any(), any(), any(), any()) } throws RuntimeException()
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate(BigDecimal("1.09"))
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate(BigDecimal("1200.00"))

        val result = service.execute(query)

        // El sistema es resiliente: Binance se cayó pero el usuario igual ve tasas
        assertThat(result.balances).isEqualTo(BinanceBalance.empty())
        assertThat(result.exchangeRate.eurUsdt).isEqualByComparingTo("1.09")
    }

    @Test
    fun `si decryptacion falla devuelve todo en default`() {
        val query = GetMarketDataQuery("enc-key", "enc-secret")
        every { cryptoPort.decrypt(any()) } returns null
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate.default()
        every { p2pRatePort.getUsdcArsRate() }      returns P2PRate.default()

        val result = service.execute(query)

        assertThat(result.balances).isEqualTo(BinanceBalance.empty())
        verify(exactly = 0) { binancePort.getBalances(any(), any()) }
    }

    @Test
    fun `si CriptoYa falla usa tasa default`() {
        val query = GetMarketDataQuery(null, null)
        every { exchangeRatePort.getEurUsdtRate() } returns ExchangeRate.default()
        every { p2pRatePort.getUsdcArsRate() } throws RuntimeException("CriptoYa timeout")

        val result = service.execute(query)

        assertThat(result.p2pRate).isEqualTo(P2PRate.default())
    }
}
