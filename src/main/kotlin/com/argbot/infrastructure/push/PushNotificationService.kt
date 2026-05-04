package com.argbot.infrastructure.push

import com.argbot.infrastructure.push.entity.PushSubscription
import com.argbot.infrastructure.push.repository.PushSubscriptionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushService
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Security

@Service
class PushNotificationService(
    @Value("\${vapid.public-key:}") private val publicKey: String,
    @Value("\${vapid.private-key:}") private val privateKey: String,
    @Value("\${vapid.subject:mailto:fgiaquintaruiz@gmail.com}") private val subject: String,
    private val subscriptionRepository: PushSubscriptionRepository
) {
    private val log = LoggerFactory.getLogger(PushNotificationService::class.java)
    private val objectMapper = ObjectMapper()
    private var pushService: PushService? = null

    @PostConstruct
    fun init() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
        if (publicKey.isBlank() || privateKey.isBlank()) {
            log.warn("[PushService] VAPID keys not configured — push notifications disabled")
            return
        }
        pushService = PushService(publicKey, privateKey, subject)
        log.info("[PushService] Initialized with subject: {}", subject)
    }

    fun subscribe(endpoint: String, p256dh: String, auth: String) {
        val existing = subscriptionRepository.findByEndpoint(endpoint)
        if (existing != null) {
            subscriptionRepository.save(existing.copy(p256dh = p256dh, auth = auth))
            log.debug("[PushService] Updated subscription: {}", endpoint.take(40))
        } else {
            subscriptionRepository.save(PushSubscription(endpoint = endpoint, p256dh = p256dh, auth = auth))
            log.debug("[PushService] New subscription: {}", endpoint.take(40))
        }
    }

    fun sendToAll(payload: PushPayload) {
        val service = pushService ?: run {
            log.debug("[PushService] Push disabled (no VAPID keys), skipping sendToAll")
            return
        }
        val json = objectMapper.writeValueAsString(payload)
        val subscriptions = subscriptionRepository.findAll()
        if (subscriptions.isEmpty()) {
            log.debug("[PushService] No subscriptions, nothing to send")
            return
        }
        for (sub in subscriptions) {
            try {
                val notification = Notification(sub.endpoint, sub.p256dh, sub.auth, json)
                val response = service.send(notification)
                val statusCode = response.statusLine.statusCode
                if (statusCode == 410) {
                    log.warn("[PushService] Subscription expired (410), removing: {}", sub.endpoint.take(40))
                    subscriptionRepository.deleteByEndpoint(sub.endpoint)
                } else if (statusCode >= 400) {
                    log.error(
                        "[PushService] Failed to send to {}: HTTP {}",
                        sub.endpoint.take(40),
                        statusCode
                    )
                } else {
                    log.debug("[PushService] Sent to endpoint: {}", sub.endpoint.take(40))
                }
            } catch (e: Exception) {
                log.error("[PushService] Exception sending to {}: {}", sub.endpoint.take(40), e.message)
            }
        }
    }
}
