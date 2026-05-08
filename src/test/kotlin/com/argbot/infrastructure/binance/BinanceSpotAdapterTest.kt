package com.argbot.infrastructure.binance

import com.argbot.infrastructure.binance.dto.BinanceAccountResponse
import com.argbot.infrastructure.binance.dto.BinanceOrderResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.RestClient
import java.math.BigDecimal

class BinanceSpotAdapterTest {

    private val prodRestClient    = mockk<RestClient>()
    private val testnetRestClient = mockk<RestClient>()
    private val adapter = BinanceSpotAdapter(prodRestClient, testnetRestClient)

    // ---- helpers ----

    private fun mockGetChainReturnsNull(client: RestClient) {
        val uriSpec      = mockk<RestClient.RequestHeadersUriSpec<*>>()
        val headersSpec  = mockk<RestClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<RestClient.ResponseSpec>()

        every { client.get() } returns uriSpec
        every { uriSpec.uri(any<String>()) } returns headersSpec
        every { headersSpec.header(any(), any()) } returns headersSpec
        every { headersSpec.retrieve() } returns responseSpec
        every { responseSpec.body(BinanceAccountResponse::class.java) } returns null
    }

    private fun mockPostChainReturnsNull(client: RestClient) {
        val bodyUriSpec  = mockk<RestClient.RequestBodyUriSpec>()
        val bodySpec     = mockk<RestClient.RequestBodySpec>()
        val responseSpec = mockk<RestClient.ResponseSpec>()

        every { client.post() } returns bodyUriSpec
        every { bodyUriSpec.uri(any<String>()) } returns bodySpec
        every { bodySpec.header(any(), any()) } returns bodySpec
        every { bodySpec.body(any<String>()) } returns bodySpec
        every { bodySpec.retrieve() } returns responseSpec
        every { responseSpec.body(BinanceOrderResponse::class.java) } returns null
    }

    // ---- tests ----

    @Test
    fun `should throw BinanceApiException when getBalances returns null body`() {
        mockGetChainReturnsNull(testnetRestClient)

        val ex = assertThrows<BinanceApiException> {
            adapter.getBalances("key", "secret", testnet = true)
        }
        assert(ex.message!!.contains("getBalances"))
    }

    @Test
    fun `should throw BinanceApiException when placeMarketOrder returns null body`() {
        mockPostChainReturnsNull(testnetRestClient)

        val ex = assertThrows<BinanceApiException> {
            adapter.placeMarketOrder("key", "secret", "EURUSDC", "SELL", BigDecimal("100"), testnet = true)
        }
        assert(ex.message!!.contains("placeMarketOrder"))
    }
}
