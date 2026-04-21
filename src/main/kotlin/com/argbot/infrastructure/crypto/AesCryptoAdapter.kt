package com.argbot.infrastructure.crypto

import com.argbot.domain.port.output.CryptoPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// Replica exacta de crypto-js AES con passphrase string.
// crypto-js usa OpenSSL EVP_BytesToKey: MD5 iterativo para derivar clave + IV desde la passphrase.
// El ciphertext viene en formato OpenSSL: Base64("Salted__" + salt[8] + cipher)
@Component
class AesCryptoAdapter(
    @Value("\${crypto.encryption-key}") private val encryptionKey: String
) : CryptoPort {

    override fun decrypt(ciphertext: String): String? = runCatching {
        val ciphertextBytes = Base64.getDecoder().decode(ciphertext)
        // Los primeros 8 bytes son la firma "Salted__", los siguientes 8 son el salt
        val salt = ciphertextBytes.copyOfRange(8, 16)
        val encrypted = ciphertextBytes.copyOfRange(16, ciphertextBytes.size)

        val (key, iv) = deriveKeyAndIv(encryptionKey.toByteArray(), salt)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }.getOrNull()

    // EVP_BytesToKey(MD5, 1 iter): genera 48 bytes = 32 key + 16 IV
    private fun deriveKeyAndIv(passphrase: ByteArray, salt: ByteArray): Pair<ByteArray, ByteArray> {
        val md5 = MessageDigest.getInstance("MD5")
        var derived = ByteArray(0)
        var block = ByteArray(0)
        while (derived.size < 48) {
            block = md5.digest(block + passphrase + salt)
            derived += block
            md5.reset()
        }
        return derived.copyOfRange(0, 32) to derived.copyOfRange(32, 48)
    }
}
