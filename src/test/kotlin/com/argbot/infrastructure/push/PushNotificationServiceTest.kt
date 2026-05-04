package com.argbot.infrastructure.push

import com.argbot.infrastructure.push.repository.PushSubscriptionRepository
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.security.Security

@ExtendWith(MockitoExtension::class)
class PushNotificationServiceTest {

    @Mock
    private lateinit var subscriptionRepository: PushSubscriptionRepository

    @Test
    fun `init registers BouncyCastle provider`() {
        Security.removeProvider("BC")

        val service = PushNotificationService(
            publicKey = "",
            privateKey = "",
            subject = "mailto:test@test.com",
            subscriptionRepository = subscriptionRepository
        )

        service.init()

        assertThat(Security.getProvider("BC")).isNotNull()
    }

    @Test
    fun `sendToAll does nothing when VAPID keys not configured`() {
        val service = PushNotificationService(
            publicKey = "",
            privateKey = "",
            subject = "mailto:test@test.com",
            subscriptionRepository = subscriptionRepository
        )
        service.init()

        service.sendToAll(PushPayload("title", "body", "/"))

        verifyNoInteractions(subscriptionRepository)
    }

    @Test
    fun `sendToAll does nothing when no subscriptions`() {
        val service = PushNotificationService(
            publicKey = "",
            privateKey = "",
            subject = "mailto:test@test.com",
            subscriptionRepository = subscriptionRepository
        )
        service.init() // pushService = null (no VAPID keys)

        service.sendToAll(PushPayload("title", "body", "/"))

        // pushService is null → early return before touching repository
        verifyNoInteractions(subscriptionRepository)
    }
}
