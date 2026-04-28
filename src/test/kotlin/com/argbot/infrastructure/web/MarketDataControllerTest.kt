package com.argbot.infrastructure.web

import com.argbot.domain.model.*
import com.argbot.domain.port.input.GetMarketDataQuery
import com.argbot.domain.port.input.GetMarketDataUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

// @WebMvcTest carga SOLO la capa web — no levanta el contexto completo de Spring.
// Es rápido porque no inicializa base de datos, servicios ni adapters externos.
@WebMvcTest(MarketDataController::class)
class MarketDataControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var getMarketData: GetMarketDataUseCase

    private val defaultMarketData = MarketData(
        balances      = ExchangeBalance.empty(),
        exchangeRate  = ExchangeRate(BigDecimal("1.09")),
        p2pRate       = P2PRate(BigDecimal("1200.00")),
        ripioRate     = P2PRate(BigDecimal("1200.00")),
        nexoRate      = P2PRate(BigDecimal("1100.00")),
        withdrawalFee = WithdrawalFee.default()
    )

    @Test
    fun `POST api-data sin body devuelve 200 con defaults`() {
        every { getMarketData.execute(GetMarketDataQuery(null, null, true)) } returns defaultMarketData

        mockMvc.post("/api/data") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.rate") { exists() }
            jsonPath("$.balances.eur") { value("0.00") }
            jsonPath("$.ripioUsdcArsRate") { exists() }
            jsonPath("$.nexoUsdcArsRate") { exists() }
        }
    }

    @Test
    fun `POST api-data con credenciales las pasa al use case`() {
        val query = GetMarketDataQuery("enc-key", "enc-secret", true)
        every { getMarketData.execute(query) } returns defaultMarketData

        mockMvc.post("/api/data") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"encKey":"enc-key","encSecret":"enc-secret"}"""
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST api-data acepta apiKey y apiSecret como aliases del frontend`() {
        // Bug: el frontend enviaba apiKey/apiSecret pero el DTO esperaba encKey/encSecret
        // Jackson no podía bindear los campos → credentials null → eur 0.00 siempre
        val query = GetMarketDataQuery("enc-key", "enc-secret", true)
        every { getMarketData.execute(query) } returns defaultMarketData

        mockMvc.post("/api/data") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"apiKey":"enc-key","apiSecret":"enc-secret","testnet":true}"""
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST api-data con testnet false pasa el flag correctamente al use case`() {
        val query = GetMarketDataQuery("enc-key", "enc-secret", false)
        every { getMarketData.execute(query) } returns defaultMarketData

        mockMvc.post("/api/data") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"apiKey":"enc-key","apiSecret":"enc-secret","testnet":false}"""
        }.andExpect {
            status { isOk() }
        }
    }
}
