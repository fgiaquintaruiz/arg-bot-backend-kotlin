package com.argbot.infrastructure.binance

import com.argbot.infrastructure.binance.dto.BinanceAccountResponse
import com.argbot.infrastructure.binance.dto.BinanceAsset
import com.argbot.infrastructure.binance.dto.BinanceOrderResponse
import com.argbot.infrastructure.binance.testsupport.mockRestClientGet
import com.argbot.infrastructure.binance.testsupport.mockRestClientPost
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Happy-path tests for BinanceSpotAdapter — transformation / parsing branches.
 *
 * Focus: assert RETURNED VALUES, not just absence of exception.
 * Error paths → BinanceSpotAdapterErrorTest.kt
 * Null-body paths → BinanceSpotAdapterTest.kt (Fase 0.5)
 */
class BinanceSpotAdapterHappyPathTest {

    // ───────────── getBalances ─────────────

    @Test
    fun `should_return_eur_and_usdc_balances_when_both_assets_present`() {
        val response = BinanceAccountResponse(
            balances = listOf(
                BinanceAsset("EUR",  "100.50", "0.00"),
                BinanceAsset("USDC", "42.00",  "0.00"),
                BinanceAsset("BTC",  "0.001",  "0.00")
            )
        )
        val client = mockRestClientGet(returnBody = response)
        val adapter = BinanceSpotAdapter(client, client)

        val result = adapter.getBalances("key", "secret", testnet = false)

        assertEquals(BigDecimal("100.50"), result.eur)
        assertEquals(BigDecimal("42.00"),  result.usdc)
    }

    @Test
    fun `should_return_zero_eur_when_eur_asset_not_in_response`() {
        val response = BinanceAccountResponse(
            balances = listOf(
                BinanceAsset("USDC", "200.00", "0.00")
            )
        )
        val client = mockRestClientGet(returnBody = response)
        val adapter = BinanceSpotAdapter(client, client)

        val result = adapter.getBalances("key", "secret", testnet = false)

        assertEquals(BigDecimal("0.00"), result.eur)
        assertEquals(BigDecimal("200.00"), result.usdc)
    }

    @Test
    fun `should_return_zero_usdc_when_usdc_asset_not_in_response`() {
        val response = BinanceAccountResponse(
            balances = listOf(
                BinanceAsset("EUR", "55.75", "0.00")
            )
        )
        val client = mockRestClientGet(returnBody = response)
        val adapter = BinanceSpotAdapter(client, client)

        val result = adapter.getBalances("key", "secret", testnet = false)

        assertEquals(BigDecimal("55.75"), result.eur)
        assertEquals(BigDecimal("0.00"),  result.usdc)
    }

    @Test
    fun `should_return_zero_balances_when_response_has_empty_balances_list`() {
        val response = BinanceAccountResponse(balances = emptyList())
        val client = mockRestClientGet(returnBody = response)
        val adapter = BinanceSpotAdapter(client, client)

        val result = adapter.getBalances("key", "secret", testnet = false)

        assertEquals(BigDecimal("0.00"), result.eur)
        assertEquals(BigDecimal("0.00"), result.usdc)
    }

    @Test
    fun `should_floor_fractional_free_balance_when_parsing_eur`() {
        // ExchangeBalance.of() applies FLOOR rounding to 2 decimal places
        val response = BinanceAccountResponse(
            balances = listOf(
                BinanceAsset("EUR",  "99.999", "0.00"),
                BinanceAsset("USDC", "10.001", "0.00")
            )
        )
        val client = mockRestClientGet(returnBody = response)
        val adapter = BinanceSpotAdapter(client, client)

        val result = adapter.getBalances("key", "secret", testnet = false)

        // FLOOR: 99.999 → 99.99, 10.001 → 10.00
        assertEquals(BigDecimal("99.99"), result.eur)
        assertEquals(BigDecimal("10.00"), result.usdc)
    }

    @Test
    fun `should_use_testnet_client_when_testnet_flag_is_true`() {
        val response = BinanceAccountResponse(
            balances = listOf(
                BinanceAsset("EUR",  "5.00",  "0.00"),
                BinanceAsset("USDC", "15.00", "0.00")
            )
        )
        val testnetClient = mockRestClientGet(returnBody = response)
        // prodClient is never called — if it were, mockk would throw UnmockedCallException
        val prodClient = mockRestClientGet(returnBody = null)
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        val result = adapter.getBalances("key", "secret", testnet = true)

        assertEquals(BigDecimal("5.00"),  result.eur)
        assertEquals(BigDecimal("15.00"), result.usdc)
    }

    // ───────────── getEurUsdtRate ─────────────

    @Test
    fun `should_return_exchange_rate_when_response_contains_price`() {
        val priceMap = mapOf("price" to "1.0845")
        val prodClient = mockRestClientGet(returnBody = priceMap)
        val adapter = BinanceSpotAdapter(prodClient, mockRestClientGet(returnBody = null))

        val result = adapter.getEurUsdtRate()

        assertEquals(BigDecimal("1.0845"), result.eurUsdt)
    }

    @Test
    fun `should_preserve_decimal_precision_when_parsing_eur_usdt_rate`() {
        val priceMap = mapOf("price" to "1.08450000")
        val prodClient = mockRestClientGet(returnBody = priceMap)
        val adapter = BinanceSpotAdapter(prodClient, mockRestClientGet(returnBody = null))

        val result = adapter.getEurUsdtRate()

        // BigDecimal preserves trailing zeros from string constructor
        assertEquals(BigDecimal("1.08450000"), result.eurUsdt)
    }

    // ───────────── placeMarketOrder ─────────────

    @Test
    fun `should_return_trade_order_with_mapped_fields_when_order_placed_successfully`() {
        val response = BinanceOrderResponse(
            orderId             = 123456789L,
            symbol              = "EURUSDC",
            status              = "FILLED",
            executedQty         = "100.00000000",
            cummulativeQuoteQty = "108.45000000"
        )
        val client = mockRestClientPost(returnBody = response)
        val adapter = BinanceSpotAdapter(client, client)

        val result = adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = false)

        assertEquals(123456789L,              result.orderId)
        assertEquals("EURUSDC",               result.symbol)
        assertEquals("FILLED",                result.status)
        assertEquals(BigDecimal("100.00000000"), result.executedQty)
        assertEquals(BigDecimal("108.45000000"), result.cumulativeQuoteQty)
    }

    @Test
    fun `should_return_trade_order_using_testnet_client_when_testnet_is_true`() {
        val response = BinanceOrderResponse(
            orderId             = 987L,
            symbol              = "EURUSDC",
            status              = "FILLED",
            executedQty         = "50.00000000",
            cummulativeQuoteQty = "54.22500000"
        )
        val testnetClient = mockRestClientPost(returnBody = response)
        val prodClient    = mockRestClientPost(returnBody = null)
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        val result = adapter.placeMarketOrder("key", "secret", "EURUSDC", "BUY", BigDecimal("50"), testnet = true)

        assertEquals(987L,                   result.orderId)
        assertEquals(BigDecimal("50.00000000"),  result.executedQty)
        assertEquals(BigDecimal("54.22500000"),  result.cumulativeQuoteQty)
    }
}
