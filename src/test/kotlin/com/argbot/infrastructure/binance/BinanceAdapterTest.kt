package com.argbot.infrastructure.binance

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient

// Tests del adapter de Binance — validan el parsing y mapeo de respuestas HTTP
class BinanceAdapterTest {

    // Nota: testear RestClient requiere un enfoque de integración o un mock server.
    // Estos tests validan la lógica de parsing que sí podemos aislar.

    @Test
    fun `BinanceBalance trunca hacia abajo (floor) no redondea`() {
        // FLOOR: 100.999 → 100.99, no 101.00
        // Regla de negocio crítica: evita errores de fondos insuficientes
        val balance = com.argbot.domain.model.BinanceBalance.of(100.999, 50.555)

        assertThat(balance.eur.toPlainString()).isEqualTo("100.99")
        assertThat(balance.usdc.toPlainString()).isEqualTo("50.55")
    }

    @Test
    fun `BinanceBalance empty devuelve ceros`() {
        val empty = com.argbot.domain.model.BinanceBalance.empty()

        assertThat(empty.eur.toPlainString()).isEqualTo("0.00")
        assertThat(empty.usdc.toPlainString()).isEqualTo("0.00")
    }
}
