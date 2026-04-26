package com.argbot.domain.port.input

import com.argbot.domain.model.MarketData

// Port IN — lo que el mundo exterior le pide al dominio.
// Es una interfaz porque el dominio no sabe (ni le importa) quién lo llama.
interface GetMarketDataUseCase {
    fun execute(query: GetMarketDataQuery): MarketData
}

data class GetMarketDataQuery(
    val encryptedApiKey: String?,
    val encryptedApiSecret: String?,
    val testnet: Boolean = true
) {
    fun hasCredentials() = !encryptedApiKey.isNullOrBlank() && !encryptedApiSecret.isNullOrBlank()
}
