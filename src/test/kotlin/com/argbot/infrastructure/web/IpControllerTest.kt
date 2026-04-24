package com.argbot.infrastructure.web

import com.argbot.domain.model.PublicIp
import com.argbot.domain.port.input.GetPublicIpUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(IpController::class)
class IpControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var getPublicIp: GetPublicIpUseCase

    @Test
    fun `GET api-ip devuelve la IP`() {
        every { getPublicIp.execute() } returns PublicIp("1.2.3.4")

        mockMvc.get("/api/ip").andExpect {
            status { isOk() }
            jsonPath("$.ip") { value("1.2.3.4") }
        }
    }
}
