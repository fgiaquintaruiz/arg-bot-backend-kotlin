package com.argbot.infrastructure.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for TradeController.extractBinanceMessage.
 *
 * Verifies that each Binance error code is mapped to an actionable message in Spanish,
 * unknown codes fall back to "Binance error <code>: <msg>",
 * and non-JSON bodies return null (caller uses e.message as fallback).
 */
class TradeControllerTest {

    private val mapper = jacksonObjectMapper()

    // TradeController is not instantiated via Spring context here — we test extractBinanceMessage in isolation.
    // executeTrade is irrelevant for this unit; we pass a mock only to satisfy the constructor.
    private val controller = TradeController(
        executeTrade = mockk(relaxed = true),
        objectMapper = mapper
    )

    @Test
    fun `extractBinanceMessage maps -1013 to minimum amount message`() {
        val body = """{"code": -1013, "msg": "Filter failure: MIN_NOTIONAL"}"""

        val result = controller.extractBinanceMessage(body)

        assertEquals(
            "Binance rechazó la operación: el monto no cumple los filtros del par (mínimo, paso o valor nocional). Probá con un monto mayor.",
            result
        )
    }

    @Test
    fun `extractBinanceMessage maps -2010 to insufficient balance message`() {
        val body = """{"code": -2010, "msg": "Account has insufficient balance for requested action."}"""

        val result = controller.extractBinanceMessage(body)

        assertEquals(
            "Binance rechazó la orden. Verificá saldo disponible y que el par esté operativo.",
            result
        )
    }

    @Test
    fun `extractBinanceMessage maps -2015 to IP whitelist and permissions message`() {
        val body = """{"code": -2015, "msg": "Invalid API-key, IP, or permissions for action."}"""

        val result = controller.extractBinanceMessage(body)

        assertEquals(
            "Binance rechazó la solicitud por permisos. Verificá que tu IP esté whitelisted en la API key y que tenga los permisos de Spot/Withdraw activos.",
            result
        )
    }

    @Test
    fun `extractBinanceMessage falls back to generic Binance error for unknown code`() {
        val body = """{"code": -1003, "msg": "Filter failure"}"""

        val result = controller.extractBinanceMessage(body)

        assertEquals("Binance error -1003: Filter failure", result)
    }

    @Test
    fun `extractBinanceMessage returns null when body is not JSON`() {
        val body = "Internal Server Error"

        val result = controller.extractBinanceMessage(body)

        assertEquals(null, result)
    }

    @Test
    fun `extractBinanceMessage returns null when body is null`() {
        val result = controller.extractBinanceMessage(null)

        assertEquals(null, result)
    }

    @Test
    fun `extractBinanceMessage returns null when body is blank`() {
        val result = controller.extractBinanceMessage("   ")

        assertEquals(null, result)
    }
}
