package com.argbot.infrastructure.adapter

import com.argbot.domain.model.P2PRate
import com.argbot.domain.port.output.P2PRatePort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import com.argbot.infrastructure.criptoya.dto.CriptoyaRateResponse
import com.argbot.infrastructure.ripio.dto.RipioTickerResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.math.RoundingMode

@ExternalApiAdapter
class MarketRateAdapter(
    @Qualifier("criptoyaRestClient") private val criptoyaClient: RestClient,
    @Qualifier("ripioRestClient") private val ripioClient: RestClient
) : P2PRatePort {

    @CircuitBreaker(name = "criptoya")
    override fun getUsdcArsRate(): P2PRate {
        val response = criptoyaClient.get()
            .uri("/api/binancep2p/usdt/ars/0.1")
            .retrieve()
            .body(CriptoyaRateResponse::class.java)!!
        return P2PRate(BigDecimal(response.ask).setScale(2, RoundingMode.HALF_UP))
    }

    @CircuitBreaker(name = "criptoya")
    override fun getNexoUsdcArsRate(): P2PRate {
        val response = criptoyaClient.get()
            .uri("/api/nexo/usdc/ars/0.1")
            .retrieve()
            .body(CriptoyaRateResponse::class.java)!!
        return P2PRate(BigDecimal(response.ask).setScale(2, RoundingMode.HALF_UP))
    }

    @CircuitBreaker(name = "ripio")
    override fun getRipioUsdcArsRate(): P2PRate {
        val response = ripioClient.get()
            .uri("/v4/public/tickers?pair=USDC_ARS")
            .retrieve()
            .body(RipioTickerResponse::class.java)!!
        val bid = response.data.firstOrNull { it.pair == "USDC_ARS" }?.bid ?: 0.0
        return P2PRate(BigDecimal(bid).setScale(2, RoundingMode.HALF_UP))
    }
}
