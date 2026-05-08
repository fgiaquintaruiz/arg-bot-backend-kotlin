package com.argbot.infrastructure.binance

import com.argbot.infrastructure.binance.testsupport.mockRestClientGet
import com.argbot.infrastructure.binance.testsupport.mockRestClientPost
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.net.SocketTimeoutException

/**
 * Error-path tests for BinanceSpotAdapter.
 *
 * Verifies that the adapter does NOT swallow HTTP/network/parsing exceptions —
 * each propagates as-is to the caller.
 *
 * Existing null-body tests live in BinanceSpotAdapterTest.kt (Fase 0.5).
 */
class BinanceSpotAdapterErrorTest {

    // ───────────── getBalances ─────────────

    @Test
    fun `getBalances propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val client = mockRestClientGet(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<HttpClientErrorException.Unauthorized> {
            adapter.getBalances("key", "secret", testnet = false)
        }
    }

    @Test
    fun `getBalances propagates HTTP 429 Too Many Requests`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val client = mockRestClientGet(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<HttpClientErrorException> {
            adapter.getBalances("key", "secret", testnet = false)
        }
    }

    @Test
    fun `getBalances propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val client = mockRestClientGet(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<HttpServerErrorException> {
            adapter.getBalances("key", "secret", testnet = false)
        }
    }

    @Test
    fun `getBalances propagates RestClientException on malformed response body`() {
        val ex = RestClientException("Malformed JSON")
        val client = mockRestClientGet(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<RestClientException> {
            adapter.getBalances("key", "secret", testnet = false)
        }
    }

    @Test
    fun `getBalances propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Connection timed out", SocketTimeoutException("timeout"))
        val client = mockRestClientGet(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<ResourceAccessException> {
            adapter.getBalances("key", "secret", testnet = false)
        }
    }

    // ───────────── getEurUsdtRate ─────────────

    @Test
    fun `getEurUsdtRate propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val prodClient = mockRestClientGet(throws = ex)
        val testnetClient = org.springframework.web.client.RestClient.builder().build() // unused
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        assertThrows<HttpClientErrorException.Unauthorized> {
            adapter.getEurUsdtRate()
        }
    }

    @Test
    fun `getEurUsdtRate propagates HTTP 429 Too Many Requests`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val prodClient = mockRestClientGet(throws = ex)
        val testnetClient = org.springframework.web.client.RestClient.builder().build()
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        assertThrows<HttpClientErrorException> {
            adapter.getEurUsdtRate()
        }
    }

    @Test
    fun `getEurUsdtRate propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.BAD_GATEWAY, "Bad Gateway",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val prodClient = mockRestClientGet(throws = ex)
        val testnetClient = org.springframework.web.client.RestClient.builder().build()
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        assertThrows<HttpServerErrorException> {
            adapter.getEurUsdtRate()
        }
    }

    @Test
    fun `getEurUsdtRate propagates RestClientException on malformed response body`() {
        val ex = RestClientException("Malformed JSON")
        val prodClient = mockRestClientGet(throws = ex)
        val testnetClient = org.springframework.web.client.RestClient.builder().build()
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        assertThrows<RestClientException> {
            adapter.getEurUsdtRate()
        }
    }

    @Test
    fun `getEurUsdtRate propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Read timed out", SocketTimeoutException("timeout"))
        val prodClient = mockRestClientGet(throws = ex)
        val testnetClient = org.springframework.web.client.RestClient.builder().build()
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        assertThrows<ResourceAccessException> {
            adapter.getEurUsdtRate()
        }
    }

    @Test
    fun `should throw BinanceApiException when getEurUsdtRate returns null body`() {
        val prodClient = mockRestClientGet(returnBody = null)
        val testnetClient = org.springframework.web.client.RestClient.builder().build()
        val adapter = BinanceSpotAdapter(prodClient, testnetClient)

        val ex = assertThrows<BinanceApiException> {
            adapter.getEurUsdtRate()
        }
        assert(ex.message?.contains("empty response body from getEurUsdtRate") == true) {
            "Expected message to contain 'empty response body from getEurUsdtRate' but was: ${ex.message}"
        }
    }

    // ───────────── placeMarketOrder ─────────────

    @Test
    fun `placeMarketOrder propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val client = mockRestClientPost(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<HttpClientErrorException.Unauthorized> {
            adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = false)
        }
    }

    @Test
    fun `placeMarketOrder propagates HTTP 429 Too Many Requests`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0), null
        )
        val client = mockRestClientPost(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<HttpClientErrorException> {
            adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = false)
        }
    }

    @Test
    fun `placeMarketOrder propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val client = mockRestClientPost(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<HttpServerErrorException> {
            adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = false)
        }
    }

    @Test
    fun `placeMarketOrder propagates RestClientException on malformed response body`() {
        val ex = RestClientException("Malformed JSON")
        val client = mockRestClientPost(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<RestClientException> {
            adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = false)
        }
    }

    @Test
    fun `placeMarketOrder propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Connection timed out", SocketTimeoutException("timeout"))
        val client = mockRestClientPost(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        assertThrows<ResourceAccessException> {
            adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = false)
        }
    }

    // ───────────── 4xx with Binance error body ─────────────

    @Test
    fun `getBalances propagates 4xx with Binance error body -2010`() {
        val body = """{"code":-2010,"msg":"Account has insufficient balance for requested action."}""".toByteArray()
        val ex = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST, "Bad Request",
            org.springframework.http.HttpHeaders.EMPTY, body, Charsets.UTF_8
        )
        val client = mockRestClientGet(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        val thrown = assertThrows<HttpClientErrorException> {
            adapter.getBalances("key", "secret", testnet = false)
        }
        assert(thrown.responseBodyAsString.contains("-2010")) {
            "Expected Binance error code -2010 in response body but got: ${thrown.responseBodyAsString}"
        }
    }

    @Test
    fun `placeMarketOrder propagates 4xx with Binance error body -2015`() {
        val body = """{"code":-2015,"msg":"Invalid API-key, IP, or permissions for action."}""".toByteArray()
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized",
            org.springframework.http.HttpHeaders.EMPTY, body, Charsets.UTF_8
        )
        val client = mockRestClientPost(throws = ex)
        val adapter = BinanceSpotAdapter(client, client)

        val thrown = assertThrows<HttpClientErrorException> {
            adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = false)
        }
        assert(thrown.responseBodyAsString.contains("-2015")) {
            "Expected Binance error code -2015 in response body but got: ${thrown.responseBodyAsString}"
        }
    }
}
