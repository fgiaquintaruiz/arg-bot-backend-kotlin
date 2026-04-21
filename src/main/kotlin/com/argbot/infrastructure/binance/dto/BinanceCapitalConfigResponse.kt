package com.argbot.infrastructure.binance.dto

data class BinanceCoinConfig(val coin: String, val networkList: List<BinanceNetwork>)
data class BinanceNetwork(val network: String, val withdrawFee: String)
