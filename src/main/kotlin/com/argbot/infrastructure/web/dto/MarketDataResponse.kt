package com.argbot.infrastructure.web.dto

import com.argbot.domain.model.MarketData
import java.math.BigDecimal

data class MarketDataResponse(
    val balances: BalancesDto,
    val rate: String,
    val usdcArsRate: String,
    val ripioUsdcArsRate: String,
    val nexoUsdcArsRate: String,
    val fees: FeesDto
) {
    companion object {
        // Mapper estático: transforma el modelo de dominio al contrato HTTP.
        // El dominio NO sabe que existe HTTP — esta transformación vive en infra.
        fun from(data: MarketData) = MarketDataResponse(
            balances = BalancesDto(
                eur  = data.balances.eur.toPlainString(),
                usdc = data.balances.usdc.toPlainString()
            ),
            rate = data.exchangeRate.eurUsdt.toPlainString(),
            usdcArsRate = data.p2pRate.usdcArs.toPlainString(),
            ripioUsdcArsRate = data.ripioRate.usdcArs.toPlainString(),
            nexoUsdcArsRate = data.nexoRate.usdcArs.toPlainString(),
            fees = FeesDto(
                withdrawalUSDC_BEP20 = data.withdrawalFee.amount,
                tradingRate = data.tradingFeeRate
            )
        )
    }
}

data class BalancesDto(val eur: String, val usdc: String)
data class FeesDto(val withdrawalUSDC_BEP20: BigDecimal, val tradingRate: BigDecimal)
