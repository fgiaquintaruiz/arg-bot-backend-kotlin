package com.argbot.infrastructure.push.repository

import com.argbot.infrastructure.push.entity.BalanceSnapshot
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceSnapshotRepository : JpaRepository<BalanceSnapshot, Long> {
    fun findTopByOrderByIdDesc(): BalanceSnapshot?
}
