package com.argbot.domain.port.output

import com.argbot.domain.model.ExchangeRate

// Strategy pattern: mañana podés agregar otro proveedor de tasa sin tocar el dominio
interface ExchangeRatePort {
    fun getEurUsdtRate(): ExchangeRate
}
