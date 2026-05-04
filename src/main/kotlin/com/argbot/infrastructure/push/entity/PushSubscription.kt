package com.argbot.infrastructure.push.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "push_subscription")
data class PushSubscription(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    val endpoint: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    val p256dh: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    val auth: String,
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
