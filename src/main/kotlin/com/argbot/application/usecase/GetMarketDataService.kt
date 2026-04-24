package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.*
import com.argbot.domain.port.input.GetMarketDataQuery
import com.argbot.domain.port.input.GetMarketDataUseCase
import com.argbot.domain.port.output.CapitalPort
import com.argbot.domain.port.output.CryptoPort
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.domain.port.output.P2PRatePort
import com.argbot.domain.port.output.SpotTradingPort

// @UseCase en vez de @Service — Screaming Architecture: el código grita lo que es.
// Facade pattern: una interfaz simple (execute) sobre múltiples llamadas a adapters.
@UseCase
class GetMarketDataService(
    private val spotTradingPort: SpotTradingPort,
    private val capitalPort: CapitalPort,
    private val exchangeRatePort: ExchangeRatePort,
    private val p2pRatePort: P2PRatePort,
    private val cryptoPort: CryptoPort
) : GetMarketDataUseCase {

    override fun execute(query: GetMarketDataQuery): MarketData {
        val (balances, withdrawalFee) = resolveCredentials(query)

        // runCatching = try-catch que devuelve Result<T>. getOrDefault = fallback sin romper el flujo.
        val exchangeRate = runCatching { exchangeRatePort.getEurUsdtRate() }
            .getOrDefault(ExchangeRate.default())

        val p2pRate = runCatching { p2pRatePort.getUsdcArsRate() }
            .getOrDefault(P2PRate.default())

        val ripioRate = runCatching { p2pRatePort.getRipioUsdcArsRate() }
            .getOrDefault(P2PRate.default())

        val nexoRate = runCatching { p2pRatePort.getNexoUsdcArsRate() }
            .getOrDefault(P2PRate.default())

        return MarketData(
            balances = balances,
            exchangeRate = exchangeRate,
            p2pRate = p2pRate,
            ripioRate = ripioRate,
            nexoRate = nexoRate,
            withdrawalFee = withdrawalFee
        )
    }

    private fun resolveCredentials(query: GetMarketDataQuery): Pair<ExchangeBalance, WithdrawalFee> {
        if (!query.hasCredentials()) return ExchangeBalance.empty() to WithdrawalFee.default()

        val apiKey    = cryptoPort.decrypt(query.encryptedApiKey!!)    ?: return ExchangeBalance.empty() to WithdrawalFee.default()
        val apiSecret = cryptoPort.decrypt(query.encryptedApiSecret!!) ?: return ExchangeBalance.empty() to WithdrawalFee.default()

        val balances = runCatching { spotTradingPort.getBalances(apiKey, apiSecret) }
            .getOrDefault(ExchangeBalance.empty())

        val fee = runCatching { capitalPort.getWithdrawalFee(apiKey, apiSecret, "USDC", "BSC") }
            .getOrDefault(WithdrawalFee.default())

        return balances to fee
    }
}
