package com.argbot.infrastructure.web.dto

import com.argbot.domain.model.MarketData
import java.math.BigDecimal

data class MarketDataResponse(
    val balances: BalancesDto,
    val rate: String,
    val usdcArsRate: String,
    val ripioUsdcArsRate: String,
    val argCriptoBrokerUsdcArsRate: String,
    val fees: FeesDto,
    val testnet: Boolean
) {
    companion object {
        fun from(data: MarketData) = MarketDataResponse(
            balances = BalancesDto(
                eur  = data.balances.eur.toPlainString(),
                usdc = data.balances.usdc.toPlainString()
            ),
            rate = data.exchangeRate.eurUsdt.toPlainString(),
            usdcArsRate = data.p2pRate.usdcArs.toPlainString(),
            ripioUsdcArsRate = data.ripioRate.usdcArs.toPlainString(),
            argCriptoBrokerUsdcArsRate = data.nexoRate.usdcArs.toPlainString(),
            fees = FeesDto(
                withdrawalUSDC_BEP20 = data.withdrawalFee.amount,
                tradingRate = data.tradingFeeRate
            ),
            testnet = data.testnet
        )
    }
}

data class BalancesDto(val eur: String, val usdc: String)
data class FeesDto(val withdrawalUSDC_BEP20: BigDecimal, val tradingRate: BigDecimal)
