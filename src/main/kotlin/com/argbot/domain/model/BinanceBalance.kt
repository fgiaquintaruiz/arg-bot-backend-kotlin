package com.argbot.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class BinanceBalance(val eur: BigDecimal, val usdc: BigDecimal) {

    companion object {
        // Floor (no round-up) — evita errores de "fondos insuficientes" al operar
        fun of(eur: Double, usdc: Double) = BinanceBalance(
            eur  = eur.toBigDecimal().setScale(2, RoundingMode.FLOOR),
            usdc = usdc.toBigDecimal().setScale(2, RoundingMode.FLOOR)
        )

        fun empty() = BinanceBalance(BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2))
    }
}
