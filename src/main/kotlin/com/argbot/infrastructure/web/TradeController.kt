package com.argbot.infrastructure.web

import com.argbot.domain.port.input.ExecuteTradeCommand
import com.argbot.domain.port.input.ExecuteTradeUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import com.argbot.infrastructure.web.dto.TradeRequest
import com.argbot.infrastructure.web.dto.TradeResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.HttpClientErrorException

data class BinanceErrorPayload(val code: Int, val msg: String)

@WebAdapter
@RequestMapping("/api")
class TradeController(
    private val executeTrade: ExecuteTradeUseCase,
    private val objectMapper: ObjectMapper
) {

    @PostMapping("/trade")
    fun trade(@RequestBody request: TradeRequest): ResponseEntity<Any> {
        return try {
            val order = executeTrade.execute(
                ExecuteTradeCommand(
                    apiKey    = request.encKey,
                    apiSecret = request.encSecret,
                    amountEur = request.amountEur,
                    testnet   = request.testnet
                )
            )
            ResponseEntity.ok(TradeResponse.from(order))
        } catch (e: Exception) {
            val errorBody = (e as? HttpClientErrorException)?.responseBodyAsString
            val msg = extractBinanceMessage(errorBody) ?: e.message ?: "Error en el cambio"
            ResponseEntity.badRequest().body(mapOf("error" to msg))
        }
    }

    internal fun extractBinanceMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null

        return try {
            val payload = objectMapper.readValue<BinanceErrorPayload>(errorBody)
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
