package com.argbot.infrastructure.adapter

import com.argbot.domain.model.P2PRate
import com.argbot.domain.port.output.P2PRatePort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import com.argbot.infrastructure.criptoya.dto.CriptoyaExchangeEntry
import com.argbot.infrastructure.criptoya.dto.CriptoyaRateResponse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.math.RoundingMode

@ExternalApiAdapter
class MarketRateAdapter(
    @Qualifier("criptoyaRestClient") private val criptoyaClient: RestClient,
    private val objectMapper: ObjectMapper
) : P2PRatePort {

    @CircuitBreaker(name = "criptoya")
    override fun getUsdcArsRate(): P2PRate {
        val response = criptoyaClient.get()
            .uri("/api/binancep2p/usdt/ars/0.1")
            .retrieve()
            .body(CriptoyaRateResponse::class.java)
                ?: throw CriptoyaApiException("empty response body from getUsdcArsRate")
        return P2PRate(BigDecimal(response.ask).setScale(2, RoundingMode.HALF_UP))
    }

    @CircuitBreaker(name = "criptoya")
    override fun getNexoUsdcArsRate(): P2PRate {
        val response = criptoyaClient.get()
            .uri("/api/nexo/usdc/ars/0.1")
            .retrieve()
            .body(CriptoyaRateResponse::class.java)
                ?: throw CriptoyaApiException("empty response body from getNexoUsdcArsRate")
        return P2PRate(BigDecimal(response.bid).setScale(2, RoundingMode.HALF_UP))
    }

    @CircuitBreaker(name = "criptoya")
    override fun getRipioUsdcArsRate(): P2PRate {
        val raw = criptoyaClient.get()
            .uri("/api/usdc/ars/0.1")
            .retrieve()
            .body(String::class.java)
                ?: throw CriptoyaApiException("empty response body from getRipioUsdcArsRate")
        val typeRef = object : TypeReference<Map<String, CriptoyaExchangeEntry>>() {}
        val exchanges: Map<String, CriptoyaExchangeEntry> = objectMapper.readValue(raw, typeRef)
        val bestBid = exchanges.values.maxOfOrNull { it.totalBid } ?: 0.0
        return P2PRate(BigDecimal(bestBid).setScale(2, RoundingMode.HALF_UP))
    }
}
