package com.argbot.domain.port.output

import com.argbot.domain.model.Withdrawal
import com.argbot.domain.model.WithdrawalFee
import java.math.BigDecimal

// Port OUT — operaciones de capital: fees, retiros, configuración de red.
// Solo disponible en producción (endpoints /sapi de Binance).
// En testnet, los adapters de este port no están disponibles.
interface CapitalPort {
    fun getWithdrawalFee(apiKey: String, apiSecret: String, coin: String, network: String, testnet: Boolean = false): WithdrawalFee
    fun submitWithdrawal(apiKey: String, apiSecret: String, address: String, amount: BigDecimal): Withdrawal
}
