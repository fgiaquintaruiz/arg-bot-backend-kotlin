package com.argbot.infrastructure.adapter

import com.argbot.infrastructure.binance.testsupport.mockRestClientGet
import com.argbot.infrastructure.criptoya.dto.CriptoyaRateResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Happy-path tests for MarketRateAdapter (criptoya P2P rate adapter).
 *
 * Focus: assert RETURNED VALUES (BigDecimal scale + rounding) and routing
 * to the correct DTO field per method (ask vs bid vs aggregated totalBid).
 *
 * Error paths → MarketRateAdapterErrorTest.kt
 */
class MarketRateAdapterHappyPathTest {

    private val mapper = jacksonObjectMapper()

    // ───────────── getUsdcArsRate (binancep2p, USDT/ARS) ─────────────

    @Test
    fun `getUsdcArsRate returns ask rounded HALF_UP scale 2`() {
        val response = CriptoyaRateResponse(ask = 1234.567, bid = 1230.0, time = 1L)
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = response), mapper)

        val rate = adapter.getUsdcArsRate()

        // 1234.567 → HALF_UP scale 2 → 1234.57
        assertEquals(BigDecimal("1234.57"), rate.usdcArs)
    }

    @Test
    fun `getUsdcArsRate uses ask field not bid`() {
        val response = CriptoyaRateResponse(ask = 1500.00, bid = 1400.00, time = 1L)
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = response), mapper)

        val rate = adapter.getUsdcArsRate()

        assertEquals(BigDecimal("1500.00"), rate.usdcArs)
    }

    // ───────────── getNexoUsdcArsRate (nexo, USDC/ARS) ─────────────

    @Test
    fun `getNexoUsdcArsRate returns bid rounded HALF_UP scale 2`() {
        val response = CriptoyaRateResponse(ask = 1300.0, bid = 1287.554, time = 1L)
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = response), mapper)

        val rate = adapter.getNexoUsdcArsRate()

        // 1287.554 → HALF_UP scale 2 → 1287.55
        assertEquals(BigDecimal("1287.55"), rate.usdcArs)
    }

    @Test
    fun `getNexoUsdcArsRate uses bid field not ask`() {
        val response = CriptoyaRateResponse(ask = 1500.00, bid = 1400.00, time = 1L)
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = response), mapper)

        val rate = adapter.getNexoUsdcArsRate()

        assertEquals(BigDecimal("1400.00"), rate.usdcArs)
    }

    // ───────────── getArgCriptoBrokerUsdcArsRate (multi-exchange aggregation) ─────────────

    @Test
    fun `getArgCriptoBrokerUsdcArsRate returns max totalBid across exchanges rounded HALF_UP scale 2`() {
        val raw = """
            {
              "ripio":   {"totalAsk": 1300.0, "totalBid": 1280.50, "time": 1},
              "letsbit": {"totalAsk": 1305.0, "totalBid": 1290.999, "time": 1},
              "buenbit": {"totalAsk": 1295.0, "totalBid": 1275.0, "time": 1}
            }
        """.trimIndent()
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = raw), mapper)

        val rate = adapter.getArgCriptoBrokerUsdcArsRate()

        // max totalBid across 3 exchanges = 1290.999 → HALF_UP scale 2 → 1291.00
        assertEquals(BigDecimal("1291.00"), rate.usdcArs)
    }

    @Test
    fun `getArgCriptoBrokerUsdcArsRate handles single exchange entry`() {
        val raw = """{"ripio": {"totalAsk": 1300.0, "totalBid": 1280.0, "time": 1}}"""
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = raw), mapper)

        val rate = adapter.getArgCriptoBrokerUsdcArsRate()

        assertEquals(BigDecimal("1280.00"), rate.usdcArs)
    }

    @Test
    fun `getArgCriptoBrokerUsdcArsRate falls back to zero when exchanges map is empty`() {
        val raw = "{}"
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = raw), mapper)

        val rate = adapter.getArgCriptoBrokerUsdcArsRate()

        // Documents existing fallback: maxOfOrNull{...} ?: 0.0 → BigDecimal("0.0").setScale(2)
        // See bug list — silent fallback to zero is an issue, but test pins current behavior.
        assertEquals(BigDecimal("0.00"), rate.usdcArs)
    }
}
