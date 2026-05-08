package com.argbot.infrastructure.binance

import com.argbot.domain.model.ExchangeBalance
import com.argbot.domain.model.ExchangeRate
import com.argbot.domain.model.TradeOrder
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.domain.port.output.SpotTradingPort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import com.argbot.infrastructure.binance.dto.BinanceAccountResponse
import com.argbot.infrastructure.binance.dto.BinanceOrderResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.client.RestClient
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.math.BigDecimal

// Adapter para endpoints /api — compatibles con testnet Y producción.
// Implementa SpotTradingPort (balances) + ExchangeRatePort (precio EUR/USDT).
// Ambos usan /api, por eso viven en el mismo adapter.
@ExternalApiAdapter
class BinanceSpotAdapter(
    @Qualifier("binanceProdRestClient")    private val prodRestClient: RestClient,
    @Qualifier("binanceTestnetRestClient") private val testnetRestClient: RestClient
) : SpotTradingPort, ExchangeRatePort {

    private fun client(testnet: Boolean) = if (testnet) testnetRestClient else prodRestClient

    @CircuitBreaker(name = "binance")
    override fun getBalances(apiKey: String, apiSecret: String, testnet: Boolean): ExchangeBalance {
        val qs = queryString()
        val response = client(testnet).get()
            .uri("/api/v3/account?$qs&signature=${sign(qs, apiSecret)}")
            .header("X-MBX-APIKEY", apiKey)
            .retrieve()
            .body(BinanceAccountResponse::class.java)
            ?: throw BinanceApiException("getBalances: respuesta vacía de Binance")

        val usdc = response.balances.find { it.asset == "USDC" }?.free?.toDouble() ?: 0.0
        val eur  = response.balances.find { it.asset == "EUR"  }?.free?.toDouble() ?: 0.0
        return ExchangeBalance.of(eur, usdc)
    }

    @CircuitBreaker(name = "binance")
    override fun getEurUsdtRate(): ExchangeRate {
        val response = prodRestClient.get()
            .uri("/api/v3/ticker/price?symbol=EURUSDT")
            .retrieve()
            .body(Map::class.java)
            ?: throw BinanceApiException("empty response body from getEurUsdtRate")
        return ExchangeRate(BigDecimal(response["price"].toString()))
    }

    @CircuitBreaker(name = "binance")
    override fun placeMarketOrder(
        apiKey: String, apiSecret: String,
        symbol: String, side: String, quantity: BigDecimal, testnet: Boolean
    ): TradeOrder {
        val qs = "symbol=$symbol&side=$side&type=MARKET&quantity=$quantity&timestamp=${System.currentTimeMillis()}"
        val signed = "$qs&signature=${sign(qs, apiSecret)}"
        val response = client(testnet).post()
            .uri("/api/v3/order?$signed")
            .header("X-MBX-APIKEY", apiKey)
            .body("")   // Binance recibe los params en query string — body vacío obligatorio en RestClient
            .retrieve()
            .body(BinanceOrderResponse::class.java)
            ?: throw BinanceApiException("placeMarketOrder: respuesta vacía de Binance")
        return TradeOrder(
            orderId            = response.orderId,
            symbol             = response.symbol,
            status             = response.status,
            executedQty        = BigDecimal(response.executedQty),
            cumulativeQuoteQty = BigDecimal(response.cummulativeQuoteQty)
        )
    }

    private fun queryString() = "timestamp=${System.currentTimeMillis()}"

    private fun sign(queryString: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(queryString.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
