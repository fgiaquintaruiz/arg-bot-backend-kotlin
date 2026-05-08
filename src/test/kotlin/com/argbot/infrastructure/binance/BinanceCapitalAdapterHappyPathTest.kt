package com.argbot.infrastructure.binance

import com.argbot.infrastructure.binance.dto.BinanceCoinConfig
import com.argbot.infrastructure.binance.dto.BinanceNetwork
import com.argbot.infrastructure.binance.dto.BinanceWithdrawResponse
import com.argbot.infrastructure.binance.testsupport.mockRestClientGet
import com.argbot.infrastructure.binance.testsupport.mockRestClientPost
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Happy-path tests for BinanceCapitalAdapter — transformation / parsing branches.
 *
 * Focus: assert RETURNED VALUES, not just absence of exception.
 * Error paths → BinanceCapitalAdapterErrorTest.kt
 * Null-body and blank-id paths → BinanceCapitalAdapterTest.kt (Fase 0.5)
 */
class BinanceCapitalAdapterHappyPathTest {

    // ───────────── getWithdrawalFee — testnet shortcut ─────────────

    @Test
    fun `should_return_hardcoded_default_fee_when_testnet_is_true`() {
        // testnet=true → returns hardcoded fee without calling the REST client
        val adapter = BinanceCapitalAdapter(mockRestClientGet(returnBody = null))

        val result = adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = true)

        assertEquals("USDC",             result.coin)
        assertEquals("BSC",              result.network)
        assertEquals(BigDecimal("0.8"),  result.amount)
    }

    // ───────────── getWithdrawalFee — production paths ─────────────

    @Test
    fun `should_return_fee_from_response_when_coin_and_network_match`() {
        val configs = arrayOf(
            BinanceCoinConfig(
                coin = "USDC",
                networkList = listOf(
                    BinanceNetwork(network = "ETH", withdrawFee = "1.50"),
                    BinanceNetwork(network = "BSC", withdrawFee = "0.29")
                )
            )
        )
        val client = mockRestClientGet(returnBody = configs)
        val adapter = BinanceCapitalAdapter(client)

        val result = adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)

        assertEquals("USDC",            result.coin)
        assertEquals("BSC",             result.network)
        assertEquals(BigDecimal("0.29"), result.amount)
    }

    @Test
    fun `should_return_first_matching_network_when_multiple_networks_exist_for_coin`() {
        val configs = arrayOf(
            BinanceCoinConfig(
                coin = "USDC",
                networkList = listOf(
                    BinanceNetwork(network = "ETH", withdrawFee = "3.00"),
                    BinanceNetwork(network = "BSC", withdrawFee = "0.29"),
                    BinanceNetwork(network = "SOL", withdrawFee = "0.10")
                )
            )
        )
        val client = mockRestClientGet(returnBody = configs)
        val adapter = BinanceCapitalAdapter(client)

        // Network ETH is requested — should pick fee 3.00, not BSC or SOL
        val result = adapter.getWithdrawalFee("key", "secret", "USDC", "ETH", testnet = false)

        assertEquals(BigDecimal("3.00"), result.amount)
    }

    @Test
    fun `should_return_default_fee_when_coin_not_found_in_response`() {
        val configs = arrayOf(
            BinanceCoinConfig(
                coin = "BTC",
                networkList = listOf(BinanceNetwork(network = "BTC", withdrawFee = "0.0001"))
            )
        )
        val client = mockRestClientGet(returnBody = configs)
        val adapter = BinanceCapitalAdapter(client)

        val result = adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)

        // coin "USDC" not in list → Elvis fallback → WithdrawalFee.default().amount = 0.80
        assertEquals("USDC",             result.coin)
        assertEquals("BSC",              result.network)
        assertEquals(BigDecimal("0.80"), result.amount)
    }

    @Test
    fun `should_return_default_fee_when_coin_found_but_network_not_in_list`() {
        val configs = arrayOf(
            BinanceCoinConfig(
                coin = "USDC",
                networkList = listOf(
                    BinanceNetwork(network = "ETH", withdrawFee = "1.50")
                )
            )
        )
        val client = mockRestClientGet(returnBody = configs)
        val adapter = BinanceCapitalAdapter(client)

        // coin matches but network "BSC" is not in networkList → Elvis fallback
        val result = adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)

        assertEquals("USDC",             result.coin)
        assertEquals("BSC",              result.network)
        assertEquals(BigDecimal("0.80"), result.amount)
    }

    @Test
    fun `should_return_default_fee_when_response_array_is_empty`() {
        val configs = emptyArray<BinanceCoinConfig>()
        val client = mockRestClientGet(returnBody = configs)
        val adapter = BinanceCapitalAdapter(client)

        val result = adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)

        assertEquals(BigDecimal("0.80"), result.amount)
    }

    // ───────────── submitWithdrawal — success path ─────────────

    @Test
    fun `should_return_withdrawal_with_id_when_response_contains_valid_id`() {
        val response = BinanceWithdrawResponse(id = "abc-def-456")
        val client = mockRestClientPost(returnBody = response)
        val adapter = BinanceCapitalAdapter(client)

        val result = adapter.submitWithdrawal("key", "secret", "0xDEADBEEF", BigDecimal("50"))

        assertEquals("abc-def-456", result.id)
    }
}
