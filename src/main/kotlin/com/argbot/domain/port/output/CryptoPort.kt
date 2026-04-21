package com.argbot.domain.port.output

interface CryptoPort {
    fun decrypt(ciphertext: String): String?
}
