package com.argbot.infrastructure.push.repository

import com.argbot.infrastructure.push.entity.PushSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface PushSubscriptionRepository : JpaRepository<PushSubscription, Long> {
    fun findByEndpoint(endpoint: String): PushSubscription?

    @Modifying
    @Transactional
    fun deleteByEndpoint(endpoint: String)
}
