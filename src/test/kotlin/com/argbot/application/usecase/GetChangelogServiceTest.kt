package com.argbot.application.usecase

import com.argbot.domain.model.Changelog
import com.argbot.domain.port.output.ChangelogPort
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetChangelogServiceTest {

    private val changelogPort = mockk<ChangelogPort>()
    private val service = GetChangelogService(changelogPort)

    @Test
    fun `devuelve el contenido del changelog`() {
        every { changelogPort.getChangelog() } returns Changelog("## v1.0\n- init")

        val result = service.execute()

        assertThat(result.content).isEqualTo("## v1.0\n- init")
    }
}
