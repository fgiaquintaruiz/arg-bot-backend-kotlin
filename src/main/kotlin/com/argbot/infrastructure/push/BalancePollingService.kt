package com.argbot.infrastructure.push

import com.argbot.domain.port.output.SpotTradingPort
import com.argbot.infrastructure.push.entity.BalanceSnapshot
import com.argbot.infrastructure.push.repository.BalanceSnapshotRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BalancePollingService(
    private val spotTradingPort: SpotTradingPort,
    private val snapshotRepository: BalanceSnapshotRepository,
    private val pushService: PushNotificationService
) {
    private val log = LoggerFactory.getLogger(BalancePollingService::class.java)

    @Value("\${binance.api-key:}")
    private lateinit var apiKey: String

    @Value("\${binance.api-secret:}")
    private lateinit var apiSecret: String

    @Value("\${binance.deposit.notify.threshold:1.0}")
    private lateinit var threshold: BigDecimal

    @Scheduled(fixedDelay = 300_000)
    fun pollBalance() {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            log.warn("[DepositPoller] Binance credentials not configured — skipping")
            return
        }
        try {
            val balance = spotTradingPort.getBalances(apiKey, apiSecret, testnet = false)
            val currentEur = balance.eur
            val lastSnapshot = snapshotRepository.findTopByOrderByIdDesc()

            if (lastSnapshot == null) {
                snapshotRepository.save(BalanceSnapshot(eurBalance = currentEur))
                log.info("[DepositPoller] Baseline saved: €{}", currentEur)
                return
            }

            val delta = currentEur - lastSnapshot.eurBalance
            if (delta > threshold) {
                log.info("[DepositPoller] Deposit detected: Δ €{} — sending push", delta)
                pushService.sendToAll(
                    PushPayload(
                        title = "Depósito recibido 💶",
                        body  = "Llegaron €${"%.2f".format(delta)} a tu cuenta Binance.",
                        url   = "/"
                    )
                )
            } else {
                log.debug("[DepositPoller] No significant deposit: Δ €{}", delta)
            }

            snapshotRepository.save(BalanceSnapshot(eurBalance = currentEur))
        } catch (e: Exception) {
            log.error("[DepositPoller] Error polling balance: {}", e.message)
        }
    }
}
