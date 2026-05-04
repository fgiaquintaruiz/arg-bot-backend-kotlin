package com.argbot.infrastructure.web

import com.argbot.infrastructure.push.PushNotificationService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(PushController::class)
class PushControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var pushService: PushNotificationService

    @Test
    fun `POST subscribe guarda la suscripcion y retorna 200`() {
        justRun { pushService.subscribe(any(), any(), any()) }

        mockMvc.post("/api/push/subscribe") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "endpoint": "https://fcm.example.com/push/abc123",
                  "keys": {"p256dh": "keyABC", "auth": "authXYZ"}
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }

        verify { pushService.subscribe("https://fcm.example.com/push/abc123", "keyABC", "authXYZ") }
    }

    @Test
    fun `POST subscribe con expirationTime null retorna 200`() {
        justRun { pushService.subscribe(any(), any(), any()) }

        mockMvc.post("/api/push/subscribe") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "endpoint": "https://fcm.example.com/push/xyz",
                  "expirationTime": null,
                  "keys": {"p256dh": "k2", "auth": "a2"}
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST notify trade-complete retorna 200 y llama sendToAll`() {
        justRun { pushService.sendToAll(any()) }

        mockMvc.post("/api/push/notify/trade-complete") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        verify { pushService.sendToAll(any()) }
    }

    @Test
    fun `POST notify withdraw-complete retorna 200 y llama sendToAll`() {
        justRun { pushService.sendToAll(any()) }

        mockMvc.post("/api/push/notify/withdraw-complete") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        verify { pushService.sendToAll(any()) }
    }
}
