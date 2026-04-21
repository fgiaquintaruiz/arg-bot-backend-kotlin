package com.argbot.infrastructure.binance.dto

data class BinanceAccountResponse(val balances: List<BinanceAsset>)
data class BinanceAsset(val asset: String, val free: String, val locked: String)
