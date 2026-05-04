package com.argbot.infrastructure.push

data class PushPayload(
    val title: String,
    val body: String,
    val url: String = "/"
)
