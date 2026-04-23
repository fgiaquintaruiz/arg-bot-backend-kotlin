package com.argbot.infrastructure.binance

import com.argbot.domain.model.ExchangeBalance
import com.argbot.domain.model.ExchangeRate
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.domain.port.output.SpotTradingPort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import com.argbot.infrastructure.binance.dto.BinanceAccountResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.client.RestClient
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.math.BigDecimal

// Adapter para endpoints /api — compatibles con testnet Y producción.
// Implementa SpotTradingPort (balances) + ExchangeRatePort (precio EUR/USDT).
// Ambos usan /api, por eso viven en el mismo adapter.
@ExternalApiAdapter
class BinanceSpotAdapter(private val restClient: RestClient) : SpotTradingPort, ExchangeRatePort {

    @CircuitBreaker(name = "binance")
    override fun getBalances(apiKey: String, apiSecret: String): ExchangeBalance {
        val qs = queryString()
        val response = restClient.get()
            .uri("/api/v3/account?$qs&signature=${sign(qs, apiSecret)}")
            .header("X-MBX-APIKEY", apiKey)
            .retrieve()
            .body(BinanceAccountResponse::class.java)!!

        val usdc = response.balances.find { it.asset == "USDC" }?.free?.toDouble() ?: 0.0
        val eur  = response.balances.find { it.asset == "EUR"  }?.free?.toDouble() ?: 0.0
        return ExchangeBalance.of(eur, usdc)
    }

    @CircuitBreaker(name = "binance")
    override fun getEurUsdtRate(): ExchangeRate {
        val response = restClient.get()
            .uri("/api/v3/ticker/price?symbol=EURUSDT")
            .retrieve()
            .body(Map::class.java)!!
        return ExchangeRate(BigDecimal(response["price"].toString()))
    }

    private fun queryString() = "timestamp=${System.currentTimeMillis()}"

    private fun sign(queryString: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(queryString.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
