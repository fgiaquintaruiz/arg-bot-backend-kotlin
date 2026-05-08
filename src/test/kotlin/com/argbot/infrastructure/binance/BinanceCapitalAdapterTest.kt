package com.argbot.infrastructure.binance

import com.argbot.infrastructure.binance.dto.BinanceCoinConfig
import com.argbot.infrastructure.binance.dto.BinanceWithdrawResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.RestClient
import java.math.BigDecimal

class BinanceCapitalAdapterTest {

    private val restClient = mockk<RestClient>()
    private val adapter    = BinanceCapitalAdapter(restClient)

    // ---- helpers ----

    private fun mockGetChainReturnsNull() {
        val uriSpec      = mockk<RestClient.RequestHeadersUriSpec<*>>()
        val headersSpec  = mockk<RestClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<RestClient.ResponseSpec>()

        every { restClient.get() } returns uriSpec
        every { uriSpec.uri(any<String>()) } returns headersSpec
        every { headersSpec.header(any(), any()) } returns headersSpec
        every { headersSpec.retrieve() } returns responseSpec
        every { responseSpec.body(Array<BinanceCoinConfig>::class.java) } returns null
    }

    private fun mockPostChainReturns(response: BinanceWithdrawResponse?) {
        val bodyUriSpec  = mockk<RestClient.RequestBodyUriSpec>()
        val bodySpec     = mockk<RestClient.RequestBodySpec>()
        val responseSpec = mockk<RestClient.ResponseSpec>()

        every { restClient.post() } returns bodyUriSpec
        every { bodyUriSpec.uri(any<String>()) } returns bodySpec
        every { bodySpec.header(any(), any()) } returns bodySpec
        every { bodySpec.body(any<String>()) } returns bodySpec
        every { bodySpec.retrieve() } returns responseSpec
        every { responseSpec.body(BinanceWithdrawResponse::class.java) } returns response
    }

    // ---- tests ----

    @Test
    fun `should throw BinanceApiException when getWithdrawalFee returns null body`() {
        mockGetChainReturnsNull()

        val ex = assertThrows<BinanceApiException> {
            adapter.getWithdrawalFee("key", "secret", "USDC", "BSC", testnet = false)
        }
        assert(ex.message!!.contains("getWithdrawalFee"))
    }

    @Test
    fun `should throw BinanceApiException when submitWithdrawal returns null body`() {
        mockPostChainReturns(null)

        val ex = assertThrows<BinanceApiException> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
        assert(ex.message!!.contains("submitWithdrawal"))
    }

    @Test
    fun `should throw BinanceApiException when submitWithdrawal returns blank id`() {
        mockPostChainReturns(BinanceWithdrawResponse(id = ""))

        val ex = assertThrows<BinanceApiException> {
            adapter.submitWithdrawal("key", "secret", "0xABC", BigDecimal("10"))
        }
        assert(ex.message!!.contains("submitWithdrawal"))
    }
}
