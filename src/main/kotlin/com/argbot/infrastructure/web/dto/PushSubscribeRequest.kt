package com.argbot.infrastructure.web.dto

data class PushSubscribeRequest(
    val endpoint: String,
    val expirationTime: Long? = null,
    val keys: PushKeys
) {
    data class PushKeys(
        val p256dh: String,
        val auth: String
    )
}
