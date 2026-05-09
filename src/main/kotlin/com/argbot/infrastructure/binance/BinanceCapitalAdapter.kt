package com.argbot.infrastructure.binance

import com.argbot.domain.model.Withdrawal
import com.argbot.domain.model.WithdrawalFee
import com.argbot.domain.port.output.CapitalPort
import com.argbot.infrastructure.binance.dto.BinanceWithdrawResponse
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import com.argbot.infrastructure.binance.dto.BinanceCoinConfig
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.argbot.domain.model.WithdrawalFee.Companion.DEFAULT_AMOUNT

// Adapter para endpoints /sapi — solo disponibles en producción, NO en testnet.
// Cuando testnet=true, devuelve el fee default sin llamar a Binance.
@ExternalApiAdapter
class BinanceCapitalAdapter(@Qualifier("binanceProdRestClient") private val restClient: RestClient) : CapitalPort {

    @CircuitBreaker(name = "binance")
    override fun getWithdrawalFee(apiKey: String, apiSecret: String, coin: String, network: String, testnet: Boolean): WithdrawalFee {
        if (testnet) return WithdrawalFee(coin, network, DEFAULT_AMOUNT)
        val qs = queryString()
        val configs = restClient.get()
            .uri("/sapi/v1/capital/config/getall?$qs&signature=${sign(qs, apiSecret)}")
            .header("X-MBX-APIKEY", apiKey)
            .retrieve()
            .body(Array<BinanceCoinConfig>::class.java)
            ?: throw BinanceApiException("getWithdrawalFee: respuesta vacía de Binance")

        val fee = configs.firstOrNull { it.coin == coin }
            ?.networkList?.firstOrNull { it.network == network }
            ?.withdrawFee?.let { BigDecimal(it) }
            ?: WithdrawalFee.default().amount

        return WithdrawalFee(coin, network, fee)
    }

    @CircuitBreaker(name = "binance")
    override fun submitWithdrawal(
        apiKey: String, apiSecret: String,
        address: String, amount: BigDecimal
    ): Withdrawal {
        val qs = "coin=USDC&network=BSC&address=$address&amount=$amount&timestamp=${System.currentTimeMillis()}"
        val signed = "$qs&signature=${sign(qs, apiSecret)}"
        val response = restClient.post()
            .uri("/sapi/v1/capital/withdraw/apply?$signed")
            .header("X-MBX-APIKEY", apiKey)
            .body("")   // Binance recibe los params en query string — body vacío obligatorio en RestClient
            .retrieve()
            .body(BinanceWithdrawResponse::class.java)
            ?: throw BinanceApiException("submitWithdrawal: respuesta vacía de Binance")
        if (response.id.isBlank()) throw BinanceApiException("submitWithdrawal: id vacío en respuesta de Binance")
        return Withdrawal(id = response.id)
    }

    private fun queryString() = "timestamp=${System.currentTimeMillis()}"

    private fun sign(queryString: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(queryString.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
