package com.argbot.application.usecase

import com.argbot.domain.model.PublicIp
import com.argbot.domain.port.output.PublicIpPort
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetPublicIpServiceTest {

    private val publicIpPort = mockk<PublicIpPort>()
    private val service = GetPublicIpService(publicIpPort)

    @Test
    fun `devuelve la IP del port`() {
        every { publicIpPort.getIp() } returns PublicIp("1.2.3.4")

        val result = service.execute()

        assertThat(result.ip).isEqualTo("1.2.3.4")
    }
}
