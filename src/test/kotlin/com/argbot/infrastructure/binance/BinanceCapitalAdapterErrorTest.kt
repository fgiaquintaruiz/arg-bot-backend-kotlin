package com.argbot.infrastructure.binance

import com.argbot.infrastructure.binance.testsupport.mockRestClientGet
import com.argbot.infrastructure.binance.testsupport.mockRestClientPost
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import java.math.BigDecimal
import java.net.SocketTimeoutException

/**
 * Error-path tests for BinanceCapitalAdapter.
 *
 * Verifies that the adapter does NOT swallow HTTP/network/parsing exceptions —
 * each propagates as-is to the caller.
 *
 * Existing null-body and blank-id tests live in BinanceCapitalAdapterTest.kt (Fase 0.5).
 *
 * Note: getWithdrawalFee with testnet=true returns a hardcoded fee and never calls the
 * REST client, so all error-path tests use testnet=false.
 */
class BinanceCapitalAdapterErrorTest {

    // ───────────── getWithdrawalFee ─────────────

    @Test
    fun `getWithdrawalFee propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val adapter = BinanceCapitalAdapter(mockRestClientGet(throws = ex))

        assertThrows<HttpClientErrorException.Unauthorized> {
            adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)
        }
    }

    @Test
    fun `getWithdrawalFee propagates HTTP 429 Too Many Requests`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val adapter = BinanceCapitalAdapter(mockRestClientGet(throws = ex))

        assertThrows<HttpClientErrorException> {
            adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)
        }
    }

    @Test
    fun `getWithdrawalFee propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = BinanceCapitalAdapter(mockRestClientGet(throws = ex))

        assertThrows<HttpServerErrorException> {
            adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)
        }
    }

    @Test
    fun `getWithdrawalFee propagates RestClientException on malformed response body`() {
        val ex = RestClientException("Malformed JSON")
        val adapter = BinanceCapitalAdapter(mockRestClientGet(throws = ex))

        assertThrows<RestClientException> {
            adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)
        }
    }

    @Test
    fun `getWithdrawalFee propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Connection timed out", SocketTimeoutException("timeout"))
        val adapter = BinanceCapitalAdapter(mockRestClientGet(throws = ex))

        assertThrows<ResourceAccessException> {
            adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)
        }
    }

    @Test
    fun `getWithdrawalFee propagates 4xx with Binance error body -2010`() {
        val body = """{"code":-2010,"msg":"Account has insufficient balance for requested action."}""".toByteArray()
        val ex = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST, "Bad Request",
            org.springframework.http.HttpHeaders.EMPTY, body, Charsets.UTF_8
        )
        val adapter = BinanceCapitalAdapter(mockRestClientGet(throws = ex))

        val thrown = assertThrows<HttpClientErrorException> {
            adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)
        }
        assert(thrown.responseBodyAsString.contains("-2010")) {
            "Expected Binance error code -2010 in response body but got: ${thrown.responseBodyAsString}"
        }
    }

    // ───────────── submitWithdrawal ─────────────

    @Test
    fun `submitWithdrawal propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val adapter = BinanceCapitalAdapter(mockRestClientPost(throws = ex))

        assertThrows<HttpClientErrorException.Unauthorized> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
    }

    @Test
    fun `submitWithdrawal propagates HTTP 429 Too Many Requests`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val adapter = BinanceCapitalAdapter(mockRestClientPost(throws = ex))

        assertThrows<HttpClientErrorException> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
    }

    @Test
    fun `submitWithdrawal propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.BAD_GATEWAY, "Bad Gateway",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = BinanceCapitalAdapter(mockRestClientPost(throws = ex))

        assertThrows<HttpServerErrorException> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
    }

    @Test
    fun `submitWithdrawal propagates RestClientException on malformed response body`() {
        val ex = RestClientException("Malformed JSON")
        val adapter = BinanceCapitalAdapter(mockRestClientPost(throws = ex))

        assertThrows<RestClientException> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
    }

    @Test
    fun `submitWithdrawal propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Read timed out", SocketTimeoutException("timeout"))
        val adapter = BinanceCapitalAdapter(mockRestClientPost(throws = ex))

        assertThrows<ResourceAccessException> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
    }

    @Test
    fun `submitWithdrawal propagates 4xx with Binance error body -2015`() {
        val body = """{"code":-2015,"msg":"Invalid API-key, IP, or permissions for action."}""".toByteArray()
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized",
            org.springframework.http.HttpHeaders.EMPTY, body, Charsets.UTF_8
        )
        val adapter = BinanceCapitalAdapter(mockRestClientPost(throws = ex))

        val thrown = assertThrows<HttpClientErrorException> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
        assert(thrown.responseBodyAsString.contains("-2015")) {
            "Expected Binance error code -2015 in response body but got: ${thrown.responseBodyAsString}"
        }
    }
}
