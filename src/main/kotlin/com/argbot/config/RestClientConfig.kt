package com.argbot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

// @Bean = Factory Method pattern: Spring llama estos métodos UNA vez y gestiona el ciclo de vida.
// RestClient.builder() = Builder pattern: construcción fluida, legible, sin constructores telescópicos.
@Configuration
class RestClientConfig {

    @Bean
    fun binanceRestClient(@Value("\${binance.base-url}") baseUrl: String): RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build()

    @Bean
    fun criptoyaRestClient(): RestClient = RestClient.builder()
        .baseUrl("https://criptoya.com")
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build()
}
