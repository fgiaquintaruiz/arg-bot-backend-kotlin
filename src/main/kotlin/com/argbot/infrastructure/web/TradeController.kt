package com.argbot.infrastructure.web

import com.argbot.domain.port.input.ExecuteTradeCommand
import com.argbot.domain.port.input.ExecuteTradeUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import com.argbot.infrastructure.binance.BinanceApiException
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
    fun trade(@RequestBody request: TradeRequest): ResponseEntity<TradeResponse> {
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
        } catch (e: HttpClientErrorException) {
            val userMessage = BinanceErrorParser.parse(e.responseBodyAsString)
                ?: "Error inesperado de Binance."
            throw BinanceApiException(
                technicalMessage = "Binance HTTP ${e.statusCode}: ${e.responseBodyAsString}",
                userMessage = userMessage,
                cause = e
            )
        }
    }
}
