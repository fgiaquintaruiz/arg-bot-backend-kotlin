package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.*
import com.argbot.domain.port.input.GetMarketDataQuery
import com.argbot.domain.port.input.GetMarketDataUseCase
import com.argbot.domain.port.output.CapitalPort
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.domain.port.output.P2PRatePort
import com.argbot.domain.port.output.SpotTradingPort
import org.slf4j.LoggerFactory

// @UseCase en vez de @Service — Screaming Architecture: el código grita lo que es.
// Facade pattern: una interfaz simple (execute) sobre múltiples llamadas a adapters.
@UseCase
class GetMarketDataService(
    private val spotTradingPort: SpotTradingPort,
    private val capitalPort: CapitalPort,
    private val exchangeRatePort: ExchangeRatePort,
    private val p2pRatePort: P2PRatePort
) : GetMarketDataUseCase {

    private val log = LoggerFactory.getLogger(GetMarketDataService::class.java)

    override fun execute(query: GetMarketDataQuery): MarketData {
        val (balances, withdrawalFee) = resolveCredentials(query)

        // runCatching = try-catch que devuelve Result<T>. getOrDefault = fallback sin romper el flujo.
        val exchangeRate = runCatching { exchangeRatePort.getEurUsdtRate() }
            .getOrDefault(ExchangeRate.default())

        val p2pRate = runCatching { p2pRatePort.getUsdcArsRate() }
            .getOrDefault(P2PRate.default())

        val ripioRate = runCatching { p2pRatePort.getArgCriptoBrokerUsdcArsRate() }
            .getOrDefault(P2PRate.default())

        val nexoRate = runCatching { p2pRatePort.getNexoUsdcArsRate() }
            .getOrDefault(P2PRate.default())

        return MarketData(
            balances = balances,
            exchangeRate = exchangeRate,
            p2pRate = p2pRate,
            ripioRate = ripioRate,
            nexoRate = nexoRate,
            withdrawalFee = withdrawalFee,
            testnet = query.testnet
        )
    }

    private fun resolveCredentials(query: GetMarketDataQuery): Pair<ExchangeBalance, WithdrawalFee> {
        if (!query.hasCredentials()) {
            log.warn("No credentials in request — returning empty balance")
            return ExchangeBalance.empty() to WithdrawalFee.default()
        }

        val apiKey    = query.apiKey!!
        val apiSecret = query.apiSecret!!

        val balances = runCatching { spotTradingPort.getBalances(apiKey, apiSecret, query.testnet) }
            .onFailure { log.error("getBalances failed (testnet={}): {}", query.testnet, it.message, it) }
            .getOrDefault(ExchangeBalance.empty())

        val fee = runCatching { capitalPort.getWithdrawalFee(apiKey, apiSecret, "USDC", "BSC", query.testnet) }
            .onFailure { log.error("getWithdrawalFee failed: {}", it.message, it) }
            .getOrDefault(WithdrawalFee.default())

        log.info("Balances resolved: eur={}, usdc={}", balances.eur, balances.usdc)
        return balances to fee
    }
}
