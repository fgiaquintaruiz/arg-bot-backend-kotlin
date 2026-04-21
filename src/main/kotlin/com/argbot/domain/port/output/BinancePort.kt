package com.argbot.domain.port.output

import com.argbot.domain.model.BinanceBalance
import com.argbot.domain.model.WithdrawalFee

// Port OUT — lo que el dominio le pide a la infraestructura.
// El dominio define la interfaz; la infra la implementa. Nunca al revés.
interface BinancePort {
    fun getBalances(apiKey: String, apiSecret: String): BinanceBalance
    fun getWithdrawalFee(apiKey: String, apiSecret: String, coin: String, network: String): WithdrawalFee
}
