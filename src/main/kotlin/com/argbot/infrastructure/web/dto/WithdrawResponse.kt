package com.argbot.infrastructure.web.dto

import com.argbot.domain.model.Withdrawal

data class WithdrawResponse(val success: Boolean, val id: String) {
    companion object {
        fun from(w: Withdrawal) = WithdrawResponse(success = true, id = w.id)
    }
}
