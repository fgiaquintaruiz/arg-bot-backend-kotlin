package com.argbot.infrastructure.push

import com.argbot.domain.model.ExchangeBalance
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.domain.port.output.SpotTradingPort
import com.argbot.infrastructure.push.entity.BalanceSnapshot
import com.argbot.infrastructure.push.repository.BalanceSnapshotRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class BalancePollingServiceTest {

    @MockkBean
    private lateinit var spotTradingPort: SpotTradingPort

    @MockkBean
    private lateinit var exchangeRatePort: ExchangeRatePort

    @MockkBean
    private lateinit var pushService: PushNotificationService

    @Autowired
    private lateinit var snapshotRepository: BalanceSnapshotRepository

    @Autowired
    private lateinit var pollingService: BalancePollingService

    @BeforeEach
    fun setUp() {
        snapshotRepository.deleteAll()
        ReflectionTestUtils.setField(pollingService, "apiKey", "test-key")
        ReflectionTestUtils.setField(pollingService, "apiSecret", "test-secret")
        ReflectionTestUtils.setField(pollingService, "threshold", BigDecimal("1.0"))
    }

    @Test
    fun `primer arranque sin snapshot — guarda baseline sin push`() {
        every { spotTradingPort.getBalances("test-key", "test-secret", testnet = false) } returns
            ExchangeBalance.of(541.90, 0.0)

        pollingService.pollBalance()

        val saved = snapshotRepository.findTopByOrderByIdDesc()
        assertThat(saved).isNotNull
        assertThat(saved!!.eurBalance).isEqualByComparingTo(BigDecimal("541.90"))
        verify(exactly = 0) { pushService.sendToAll(any()) }
    }

    @Test
    fun `deposito mayor al threshold — envia push y actualiza snapshot`() {
        snapshotRepository.save(BalanceSnapshot(eurBalance = BigDecimal("541.90")))
        every { spotTradingPort.getBalances("test-key", "test-secret", testnet = false) } returns
            ExchangeBalance.of(660.00, 0.0)
        justRun { pushService.sendToAll(any()) }

        pollingService.pollBalance()

        verify(exactly = 1) { pushService.sendToAll(any()) }
        val saved = snapshotRepository.findTopByOrderByIdDesc()
        assertThat(saved!!.eurBalance).isEqualByComparingTo(BigDecimal("660.00"))
    }

    @Test
    fun `deposito menor al threshold — no envia push`() {
        snapshotRepository.save(BalanceSnapshot(eurBalance = BigDecimal("541.90")))
        every { spotTradingPort.getBalances("test-key", "test-secret", testnet = false) } returns
            ExchangeBalance.of(542.00, 0.0)

        pollingService.pollBalance()

        verify(exactly = 0) { pushService.sendToAll(any()) }
    }

    @Test
    fun `apiKey vacio — retorna sin ejecutar`() {
        ReflectionTestUtils.setField(pollingService, "apiKey", "")

        pollingService.pollBalance()

        verify(exactly = 0) { spotTradingPort.getBalances(any(), any(), any()) }
    }
}
