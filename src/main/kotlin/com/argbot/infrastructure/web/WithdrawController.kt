package com.argbot.infrastructure.web

import com.argbot.domain.port.input.WithdrawUsdcCommand
import com.argbot.domain.port.input.WithdrawUsdcUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import com.argbot.infrastructure.web.dto.WithdrawRequest
import com.argbot.infrastructure.web.dto.WithdrawResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebAdapter
@RequestMapping("/api")
class WithdrawController(private val withdrawUsdc: WithdrawUsdcUseCase) {

    @PostMapping("/withdraw")
    fun withdraw(@RequestBody request: WithdrawRequest): ResponseEntity<Any> {
        return try {
            val withdrawal = withdrawUsdc.execute(
                WithdrawUsdcCommand(
                    apiKey    = request.encKey,
                    apiSecret = request.encSecret,
                    address   = request.address,
                    amountUsdc = request.amountUsdc
                )
            )
            ResponseEntity.ok(WithdrawResponse.from(withdrawal))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Fallo en el retiro")))
        }
    }
}
