package com.argbot.infrastructure.binance

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for BinanceErrorParser.parse.
 *
 * Verifies that each Binance error code is mapped to an actionable message in Spanish,
 * unknown codes fall back to "Binance error <code>: <msg>",
 * and non-JSON or blank bodies return null (caller uses e.message as fallback).
 */
class BinanceErrorParserTest {

    @Test
    fun `parse maps -1013 to minimum amount message`() {
        val body = """{"code": -1013, "msg": "Filter failure: MIN_NOTIONAL"}"""

        val result = BinanceErrorParser.parse(body)

        assertEquals(
            "Binance rechazó la operación: el monto no cumple los filtros del par (mínimo, paso o valor nocional). Probá con un monto mayor.",
            result
        )
    }

    @Test
    fun `parse maps -2010 to insufficient balance message`() {
        val body = """{"code": -2010, "msg": "Account has insufficient balance for requested action."}"""

        val result = BinanceErrorParser.parse(body)

        assertEquals(
            "Binance rechazó la orden. Verificá saldo disponible y que el par esté operativo.",
            result
        )
    }

    @Test
    fun `parse maps -2015 to IP whitelist and permissions message`() {
        val body = """{"code": -2015, "msg": "Invalid API-key, IP, or permissions for action."}"""

        val result = BinanceErrorParser.parse(body)

        assertEquals(
            "Binance rechazó la solicitud por permisos. Verificá que tu IP esté whitelisted en la API key y que tenga los permisos de Spot/Withdraw activos.",
            result
        )
    }

    @Test
    fun `parse falls back to generic Binance error for unknown code`() {
        val body = """{"code": -1003, "msg": "Filter failure"}"""

        val result = BinanceErrorParser.parse(body)

        assertEquals("Binance error -1003: Filter failure", result)
    }

    @Test
    fun `parse returns null when body is not JSON`() {
        val body = "Internal Server Error"

        val result = BinanceErrorParser.parse(body)

        assertEquals(null, result)
    }

    @Test
    fun `parse returns null when body is null`() {
        val result = BinanceErrorParser.parse(null)

        assertEquals(null, result)
    }

    @Test
    fun `parse returns null when body is blank`() {
        val result = BinanceErrorParser.parse("   ")

        assertEquals(null, result)
    }
}
