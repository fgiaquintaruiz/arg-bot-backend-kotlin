package com.argbot.infrastructure.web

import com.argbot.domain.port.input.ExecuteTradeCommand
import com.argbot.domain.port.input.ExecuteTradeUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import com.argbot.infrastructure.binance.BinanceErrorParser
import com.argbot.infrastructure.web.dto.TradeRequest
import com.argbot.infrastructure.web.dto.TradeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.HttpClientErrorException

@WebAdapter
@RequestMapping("/api")
class TradeController(private val executeTrade: ExecuteTradeUseCase) {

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
            val msg = BinanceErrorParser.parse(errorBody) ?: e.message ?: "Error en el cambio"
            ResponseEntity.badRequest().body(mapOf("error" to msg))
        }
    }
}
