package com.argbot.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class ExchangeBalance(val eur: BigDecimal, val usdc: BigDecimal) {

    companion object {
        // Floor (no round-up) — evita errores de "fondos insuficientes" al operar
        fun of(eur: Double, usdc: Double) = ExchangeBalance(
            eur  = eur.toBigDecimal().setScale(2, RoundingMode.FLOOR),
            usdc = usdc.toBigDecimal().setScale(2, RoundingMode.FLOOR)
        )

        fun empty() = ExchangeBalance(BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2))
    }
}
