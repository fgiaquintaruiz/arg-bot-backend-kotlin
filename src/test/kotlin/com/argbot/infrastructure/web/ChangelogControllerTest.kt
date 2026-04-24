package com.argbot.infrastructure.web

import com.argbot.domain.model.Changelog
import com.argbot.domain.port.input.GetChangelogUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ChangelogController::class)
class ChangelogControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var getChangelog: GetChangelogUseCase

    @Test
    fun `GET api-changelog devuelve el contenido`() {
        every { getChangelog.execute() } returns Changelog("## v1.0\n- init")

        mockMvc.get("/api/changelog").andExpect {
            status { isOk() }
            jsonPath("$.content") { value("## v1.0\n- init") }
        }
    }
}
