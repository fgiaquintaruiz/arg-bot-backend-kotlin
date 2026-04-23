package com.argbot.infrastructure.binance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExchangeBalanceTest {

    @Test
    fun `ExchangeBalance trunca hacia abajo (floor) no redondea`() {
        // FLOOR: 100.999 → 100.99, no 101.00
        // Regla de negocio crítica: evita errores de fondos insuficientes
        val balance = com.argbot.domain.model.ExchangeBalance.of(100.999, 50.555)

        assertThat(balance.eur.toPlainString()).isEqualTo("100.99")
        assertThat(balance.usdc.toPlainString()).isEqualTo("50.55")
    }

    @Test
    fun `ExchangeBalance empty devuelve ceros`() {
        val empty = com.argbot.domain.model.ExchangeBalance.empty()

        assertThat(empty.eur.toPlainString()).isEqualTo("0.00")
        assertThat(empty.usdc.toPlainString()).isEqualTo("0.00")
    }
}
