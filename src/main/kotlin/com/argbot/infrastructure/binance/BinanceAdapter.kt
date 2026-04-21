package com.argbot.infrastructure.binance

import com.argbot.domain.model.BinanceBalance
import com.argbot.domain.model.ExchangeRate
import com.argbot.domain.model.WithdrawalFee
import com.argbot.domain.port.output.BinancePort
import com.argbot.domain.port.output.ExchangeRatePort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import com.argbot.infrastructure.binance.dto.BinanceAccountResponse
import com.argbot.infrastructure.binance.dto.BinanceCoinConfig
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

// Un adapter que implementa DOS ports: BinancePort + ExchangeRatePort.
// SRP: ambos ports son responsabilidad de Binance como fuente de datos.
// Si el día de mañana la tasa viene de otra fuente, solo cambiás ExchangeRatePort.
@ExternalApiAdapter
class BinanceAdapter(private val binanceRestClient: RestClient) : BinancePort, ExchangeRatePort {

    @CircuitBreaker(name = "binance")
    override fun getBalances(apiKey: String, apiSecret: String): BinanceBalance {
        val qs = queryString()
        val response = binanceRestClient.get()
            .uri("/api/v3/account?$qs&signature=${sign(qs, apiSecret)}")
            .header("X-MBX-APIKEY", apiKey)
            .retrieve()
            .body(BinanceAccountResponse::class.java)!!

        val usdc = response.balances.find { it.asset == "USDC" }?.free?.toDouble() ?: 0.0
        val eur  = response.balances.find { it.asset == "EUR"  }?.free?.toDouble() ?: 0.0
        return BinanceBalance.of(eur, usdc)
    }

    @CircuitBreaker(name = "binance")
    override fun getWithdrawalFee(apiKey: String, apiSecret: String, coin: String, network: String): WithdrawalFee {
        val qs = queryString()
        val configs = binanceRestClient.get()
            .uri("/sapi/v1/capital/config/getall?$qs&signature=${sign(qs, apiSecret)}")
            .header("X-MBX-APIKEY", apiKey)
            .retrieve()
            .body(Array<BinanceCoinConfig>::class.java)!!

        // firstOrNull + let = Streams filter+map en una línea. Kotlin idiomático.
        val fee = configs.firstOrNull { it.coin == coin }
            ?.networkList?.firstOrNull { it.network == network }
            ?.withdrawFee?.let { BigDecimal(it) }
            ?: BigDecimal("0.80")

        return WithdrawalFee(coin, network, fee)
    }

    @CircuitBreaker(name = "binance")
    override fun getEurUsdtRate(): ExchangeRate {
        val response = binanceRestClient.get()
            .uri("/api/v3/ticker/price?symbol=EURUSDT")
            .retrieve()
            .body(Map::class.java)!!
        return ExchangeRate(BigDecimal(response["price"].toString()))
    }

    private fun queryString() = "timestamp=${System.currentTimeMillis()}"

    // HMAC-SHA256 — firma cada request a Binance. Sin esto, la API rechaza las llamadas autenticadas.
    private fun sign(queryString: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(queryString.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
