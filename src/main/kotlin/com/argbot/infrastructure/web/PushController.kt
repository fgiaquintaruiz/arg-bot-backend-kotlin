package com.argbot.infrastructure.web

import com.argbot.infrastructure.annotation.WebAdapter
import com.argbot.infrastructure.push.PushNotificationService
import com.argbot.infrastructure.push.PushPayload
import com.argbot.infrastructure.web.dto.PushSubscribeRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebAdapter
@RequestMapping("/api/push")
class PushController(private val pushService: PushNotificationService) {

    @PostMapping("/subscribe")
    fun subscribe(@RequestBody request: PushSubscribeRequest): ResponseEntity<Map<String, Boolean>> {
        pushService.subscribe(request.endpoint, request.keys.p256dh, request.keys.auth)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    @PostMapping("/notify/trade-complete")
    fun notifyTradeComplete(): ResponseEntity<Void> {
        pushService.sendToAll(PushPayload(
            title = "Cambio completado ✅",
            body = "Tu cambio EUR → USDC fue ejecutado.",
            url = "/trade"
        ))
        return ResponseEntity.ok().build()
    }

    @PostMapping("/notify/withdraw-complete")
    fun notifyWithdrawComplete(): ResponseEntity<Void> {
        pushService.sendToAll(PushPayload(
            title = "USDC enviado 🚀",
            body = "Tu USDC está en camino a la wallet destino.",
            url = "/withdraw"
        ))
        return ResponseEntity.ok().build()
    }
}
