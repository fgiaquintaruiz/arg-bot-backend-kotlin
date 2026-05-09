package com.argbot.infrastructure.binance

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

private data class BinanceErrorPayload(val code: Int, val msg: String)
private val objectMapper = jacksonObjectMapper()

object BinanceErrorParser {
    fun parse(rawErrorBody: String?): String? {
        if (rawErrorBody.isNullOrBlank()) return null
        return try {
            val payload = objectMapper.readValue<BinanceErrorPayload>(rawErrorBody)
            when (payload.code) {
                -1013 -> "Binance rechazó la operación: el monto no cumple los filtros del par (mínimo, paso o valor nocional). Probá con un monto mayor."
                -2010 -> "Binance rechazó la orden. Verificá saldo disponible y que el par esté operativo."
                -2015 -> "Binance rechazó la solicitud por permisos. Verificá que tu IP esté whitelisted en la API key y que tenga los permisos de Spot/Withdraw activos."
                else  -> "Binance error ${payload.code}: ${payload.msg}"
            }
        } catch (e: Exception) {
            null
        }
    }
}
