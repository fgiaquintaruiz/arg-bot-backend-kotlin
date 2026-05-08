package com.argbot.infrastructure.adapter

import com.argbot.infrastructure.binance.testsupport.mockRestClientGet
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import java.net.SocketTimeoutException

/**
 * Error-path tests for MarketRateAdapter.
 *
 * Verifies that the adapter does NOT swallow HTTP/network/parsing exceptions —
 * each propagates as-is to the caller (consistent with binance adapters).
 *
 * Null-body cases produce raw NullPointerException due to the `body()!!` smell —
 * tracked in bug list (Tanda B1, fase 1, paso 2). Tests pin current behavior.
 */
class MarketRateAdapterErrorTest {

    private val mapper = jacksonObjectMapper()

    // ───────────── getUsdcArsRate ─────────────

    @Test
    fun `getUsdcArsRate propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<HttpClientErrorException.Unauthorized> { adapter.getUsdcArsRate() }
    }

    @Test
    fun `getUsdcArsRate propagates HTTP 429 Too Many Requests`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<HttpClientErrorException> { adapter.getUsdcArsRate() }
    }

    @Test
    fun `getUsdcArsRate propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<HttpServerErrorException> { adapter.getUsdcArsRate() }
    }

    @Test
    fun `getUsdcArsRate propagates RestClientException on malformed body`() {
        val ex = RestClientException("Malformed JSON")
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<RestClientException> { adapter.getUsdcArsRate() }
    }

    @Test
    fun `getUsdcArsRate propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Connection timed out", SocketTimeoutException("timeout"))
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<ResourceAccessException> { adapter.getUsdcArsRate() }
    }

    @Test
    fun `getUsdcArsRate throws NullPointerException when body is null (body double-bang smell)`() {
        // Pins current behavior: body()!! produces raw NPE rather than a domain exception.
        // See bug list entry MarketRateAdapter:27.
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = null), mapper)

        assertThrows<NullPointerException> { adapter.getUsdcArsRate() }
    }

    // ───────────── getNexoUsdcArsRate ─────────────

    @Test
    fun `getNexoUsdcArsRate propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<HttpClientErrorException.Unauthorized> { adapter.getNexoUsdcArsRate() }
    }

    @Test
    fun `getNexoUsdcArsRate propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.BAD_GATEWAY, "Bad Gateway",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<HttpServerErrorException> { adapter.getNexoUsdcArsRate() }
    }

    @Test
    fun `getNexoUsdcArsRate propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Read timed out", SocketTimeoutException("timeout"))
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<ResourceAccessException> { adapter.getNexoUsdcArsRate() }
    }

    @Test
    fun `getNexoUsdcArsRate throws NullPointerException when body is null (body double-bang smell)`() {
        // See bug list entry MarketRateAdapter:36.
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = null), mapper)

        assertThrows<NullPointerException> { adapter.getNexoUsdcArsRate() }
    }

    // ───────────── getRipioUsdcArsRate ─────────────

    @Test
    fun `getRipioUsdcArsRate propagates HTTP 401 Unauthorized`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "Unauthorized",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<HttpClientErrorException.Unauthorized> { adapter.getRipioUsdcArsRate() }
    }

    @Test
    fun `getRipioUsdcArsRate propagates HTTP 5xx server error`() {
        val ex = HttpServerErrorException.create(
            HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
            org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null
        )
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<HttpServerErrorException> { adapter.getRipioUsdcArsRate() }
    }

    @Test
    fun `getRipioUsdcArsRate propagates ResourceAccessException on timeout`() {
        val ex = ResourceAccessException("Connect timed out", SocketTimeoutException("timeout"))
        val adapter = MarketRateAdapter(mockRestClientGet(throws = ex), mapper)

        assertThrows<ResourceAccessException> { adapter.getRipioUsdcArsRate() }
    }

    @Test
    fun `getRipioUsdcArsRate propagates Jackson exception on malformed JSON body`() {
        // String body returns OK, but objectMapper.readValue fails to deserialize.
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = "not-a-valid-json"), mapper)

        assertThrows<Exception> { adapter.getRipioUsdcArsRate() }
    }

    @Test
    fun `getRipioUsdcArsRate throws Jackson MismatchedInputException on JSON-array body when Map expected`() {
        // Body is a JSON array, but readValue expects Map<String, CriptoyaExchangeEntry>.
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = "[]"), mapper)

        assertThrows<MismatchedInputException> { adapter.getRipioUsdcArsRate() }
    }

    @Test
    fun `getRipioUsdcArsRate throws NullPointerException when body is null (body double-bang smell)`() {
        // See bug list entry MarketRateAdapter:45.
        val adapter = MarketRateAdapter(mockRestClientGet(returnBody = null), mapper)

        assertThrows<NullPointerException> { adapter.getRipioUsdcArsRate() }
    }
}
