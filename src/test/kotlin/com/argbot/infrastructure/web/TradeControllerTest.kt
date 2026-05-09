package com.argbot.infrastructure.web

import com.argbot.domain.port.input.ExecuteTradeUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

/**
 * Integration-style tests for TradeController endpoints.
 * BinanceErrorParser logic is tested separately in BinanceErrorParserTest.
 */
@WebMvcTest(TradeController::class)
class TradeControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var executeTrade: ExecuteTradeUseCase

    @Test
    fun `POST api-trade returns 400 when use case throws generic exception`() {
        every { executeTrade.execute(any()) } answers { throw RuntimeException("algo falló") }

        mockMvc.post("/api/trade") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"encKey":"k","encSecret":"s","amountEur":100,"testnet":false}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("algo falló") }
        }
    }
}
