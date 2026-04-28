package com.argbot.infrastructure.web

import com.argbot.domain.port.input.GetMarketDataQuery
import com.argbot.domain.port.input.GetMarketDataUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import com.argbot.infrastructure.web.dto.MarketDataRequest
import com.argbot.infrastructure.web.dto.MarketDataResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

// @WebAdapter = @RestController — mismo efecto, intención arquitectónica clara.
// Este adapter SOLO traduce HTTP → dominio y dominio → HTTP. Sin lógica de negocio.
@WebAdapter
@RequestMapping("/api")
class MarketDataController(private val getMarketData: GetMarketDataUseCase) {

    @PostMapping("/data")
    fun getData(@RequestBody(required = false) request: MarketDataRequest?): MarketDataResponse {
        val query = GetMarketDataQuery(
            apiKey    = request?.encKey,
            apiSecret = request?.encSecret,
            testnet   = request?.testnet ?: true
        )
        return MarketDataResponse.from(getMarketData.execute(query))
    }
}
