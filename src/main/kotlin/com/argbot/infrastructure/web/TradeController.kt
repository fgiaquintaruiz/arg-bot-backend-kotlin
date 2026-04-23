package com.argbot.infrastructure.web

import com.argbot.domain.port.input.ExecuteTradeCommand
import com.argbot.domain.port.input.ExecuteTradeUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import com.argbot.infrastructure.web.dto.TradeRequest
import com.argbot.infrastructure.web.dto.TradeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebAdapter
@RequestMapping("/api")
class TradeController(private val executeTrade: ExecuteTradeUseCase) {

    @PostMapping("/trade")
    fun trade(@RequestBody request: TradeRequest): ResponseEntity<Any> {
        return try {
            val order = executeTrade.execute(
                ExecuteTradeCommand(
                    encryptedApiKey    = request.encKey,
                    encryptedApiSecret = request.encSecret,
                    amountEur          = request.amountEur
                )
            )
            ResponseEntity.ok(TradeResponse.from(order))
        } catch (e: Exception) {
            val msg = e.message?.let { extractBinanceMessage(it) } ?: "Error en el cambio"
            ResponseEntity.badRequest().body(mapOf("error" to msg))
        }
    }

    private fun extractBinanceMessage(msg: String): String =
        if (msg.contains("-1013")) "Mínimo ~10 EUR requerido." else msg
}
