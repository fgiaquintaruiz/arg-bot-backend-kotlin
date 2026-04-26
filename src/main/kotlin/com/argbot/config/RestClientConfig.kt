package com.argbot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    fun binanceProdRestClient(): RestClient = RestClient.builder()
        .baseUrl("https://api.binance.com")
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build()

    @Bean
    fun binanceTestnetRestClient(): RestClient = RestClient.builder()
        .baseUrl("https://testnet.binance.vision")
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build()

    @Bean
    fun criptoyaRestClient(): RestClient = RestClient.builder()
        .baseUrl("https://criptoya.com")
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build()

}
