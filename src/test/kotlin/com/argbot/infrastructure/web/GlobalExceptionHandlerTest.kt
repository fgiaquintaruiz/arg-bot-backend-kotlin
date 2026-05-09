package com.argbot.infrastructure.web

import com.argbot.infrastructure.binance.BinanceApiException
import com.argbot.infrastructure.adapter.CriptoyaApiException
import com.argbot.infrastructure.ip.IpifyApiException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Unit tests for GlobalExceptionHandler.
 *
 * Uses a standalone MockMvc setup with a fake controller that throws exceptions on demand.
 * This avoids loading the full Spring context while still exercising the @ControllerAdvice.
 */
class GlobalExceptionHandlerTest {

    @RestController
    class FakeController {
        var exceptionToThrow: Exception? = null

        @GetMapping("/test-exception")
        fun trigger(): String {
            exceptionToThrow?.let { throw it }
            return "ok"
        }
    }

    private val fakeController = FakeController()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(fakeController)
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun `CallNotPermittedException returns 503 with CIRCUIT_OPEN code and Retry-After header`() {
        val cb = mockk<CircuitBreaker>(relaxed = true)
        every { cb.name } returns "test"
        fakeController.exceptionToThrow = CallNotPermittedException.createCallNotPermittedException(cb)

        mockMvc.get("/test-exception") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isServiceUnavailable() }
            header { string("Retry-After", "30") }
            jsonPath("$.code") { value("CIRCUIT_OPEN") }
            jsonPath("$.retryAfter") { value(30) }
        }
    }

    @Test
    fun `BinanceApiException returns 502 with BINANCE_ERROR code and userMessage`() {
        fakeController.exceptionToThrow = BinanceApiException(
            technicalMessage = "Binance HTTP 400",
            userMessage = "Binance rechazó la operación: monto insuficiente."
        )

        mockMvc.get("/test-exception").andExpect {
            status { isBadGateway() }
            jsonPath("$.code") { value("BINANCE_ERROR") }
            jsonPath("$.error") { value("Binance rechazó la operación: monto insuficiente.") }
        }
    }

    @Test
    fun `CriptoyaApiException returns 502 with CRIPTOYA_ERROR code`() {
        fakeController.exceptionToThrow = CriptoyaApiException("no data from criptoya")

        mockMvc.get("/test-exception").andExpect {
            status { isBadGateway() }
            jsonPath("$.code") { value("CRIPTOYA_ERROR") }
            jsonPath("$.error") { value("No hay datos disponibles del proveedor de tasas en este momento.") }
        }
    }

    @Test
    fun `IpifyApiException returns 502 with IPIFY_ERROR code`() {
        fakeController.exceptionToThrow = IpifyApiException("could not reach ipify")

        mockMvc.get("/test-exception").andExpect {
            status { isBadGateway() }
            jsonPath("$.code") { value("IPIFY_ERROR") }
            jsonPath("$.error") { value("No se pudo obtener la IP pública.") }
        }
    }

    @Test
    fun `IllegalArgumentException returns 400 with BAD_REQUEST code`() {
        fakeController.exceptionToThrow = IllegalArgumentException("campo requerido faltante")

        mockMvc.get("/test-exception").andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("BAD_REQUEST") }
            jsonPath("$.error") { value("campo requerido faltante") }
        }
    }

    @Test
    fun `generic Exception returns 500 with INTERNAL_ERROR code`() {
        fakeController.exceptionToThrow = RuntimeException("error no esperado")

        mockMvc.get("/test-exception").andExpect {
            status { isInternalServerError() }
            jsonPath("$.code") { value("INTERNAL_ERROR") }
            jsonPath("$.error") { value("Error interno del servidor.") }
        }
    }
}
