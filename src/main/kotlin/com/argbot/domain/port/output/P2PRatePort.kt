package com.argbot.domain.port.output

import com.argbot.domain.model.P2PRate

interface P2PRatePort {
    fun getUsdcArsRate(): P2PRate
}
