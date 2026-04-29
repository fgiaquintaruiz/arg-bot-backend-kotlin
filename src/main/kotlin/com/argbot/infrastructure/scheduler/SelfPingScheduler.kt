package com.argbot.infrastructure.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class SelfPingScheduler {
    private val log = LoggerFactory.getLogger(SelfPingScheduler::class.java)
    private val restClient = RestClient.create()

    @Scheduled(fixedDelay = 540000)
    fun ping() {
        try {
            restClient.get()
                .uri("https://arg-bot-backend-kotlin.onrender.com/api/version")
                .retrieve()
                .toBodilessEntity()
            log.debug("Self-ping OK")
        } catch (ex: Exception) {
            log.error("Self-ping failed: {}", ex.message)
        }
    }
}
