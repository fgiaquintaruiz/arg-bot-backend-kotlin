package com.argbot.infrastructure.criptoya

import com.argbot.domain.model.P2PRate
import com.argbot.domain.port.output.P2PRatePort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import com.argbot.infrastructure.criptoya.dto.CriptoyaP2PResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.math.RoundingMode

@ExternalApiAdapter
class CriptoyaAdapter(private val criptoyaRestClient: RestClient) : P2PRatePort {

    @CircuitBreaker(name = "criptoya")
    override fun getUsdcArsRate(): P2PRate {
        val response = criptoyaRestClient.get()
            .uri("/api/binancep2p/usdt/ars/0.1")
            .retrieve()
            .body(CriptoyaP2PResponse::class.java)!!
        return P2PRate(BigDecimal(response.ask).setScale(2, RoundingMode.HALF_UP))
    }
}
