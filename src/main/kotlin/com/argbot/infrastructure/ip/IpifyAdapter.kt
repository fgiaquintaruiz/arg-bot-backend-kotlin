package com.argbot.infrastructure.ip

import com.argbot.domain.model.PublicIp
import com.argbot.domain.port.output.PublicIpPort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.client.RestClient

@ExternalApiAdapter
class IpifyAdapter(
    private val restClient: RestClient = RestClient.builder().baseUrl("https://api.ipify.org").build()
) : PublicIpPort {

    @CircuitBreaker(name = "ipify")
    override fun getIp(): PublicIp {
        val ip = restClient.get()
            .uri("/")
            .retrieve()
            .body(String::class.java)
                ?: throw IpifyApiException("empty response body from getIp")
        return PublicIp(ip)
    }
}
