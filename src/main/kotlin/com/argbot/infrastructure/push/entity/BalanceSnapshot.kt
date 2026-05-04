package com.argbot.infrastructure.push.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "balance_snapshot")
data class BalanceSnapshot(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "eur_balance", nullable = false, precision = 18, scale = 8)
    val eurBalance: BigDecimal,
    @Column(name = "recorded_at")
    val recordedAt: LocalDateTime = LocalDateTime.now()
)
