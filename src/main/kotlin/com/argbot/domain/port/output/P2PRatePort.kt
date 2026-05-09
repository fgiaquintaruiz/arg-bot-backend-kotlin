package com.argbot.domain.port.output

import com.argbot.domain.model.P2PRate

interface P2PRatePort {
    fun getUsdcArsRate(): P2PRate                    // Binance P2P
    fun getArgCriptoBrokerUsdcArsRate(): P2PRate     // ArgCriptoBroker (multi-exchange aggregation)
    fun getNexoUsdcArsRate(): P2PRate                // Nexo
}
