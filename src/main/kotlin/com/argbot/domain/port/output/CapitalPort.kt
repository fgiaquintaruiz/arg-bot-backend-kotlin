package com.argbot.domain.port.output

import com.argbot.domain.model.WithdrawalFee

// Port OUT — operaciones de capital: fees, retiros, configuración de red.
// Solo disponible en producción (endpoints /sapi de Binance).
// En testnet, los adapters de este port no están disponibles.
interface CapitalPort {
    fun getWithdrawalFee(apiKey: String, apiSecret: String, coin: String, network: String): WithdrawalFee
}
