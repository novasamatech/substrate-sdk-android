package io.novasama.substrate_sdk_android.encrypt.json.coders.type.cryptor

import io.novasama.substrate_sdk_android.encrypt.json.coders.type.JsonCryptor
import io.novasama.substrate_sdk_android.encrypt.json.coders.type.JsonTypeDecoder
import io.novasama.substrate_sdk_android.encrypt.json.coders.type.JsonTypeEncoder
import io.novasama.substrate_sdk_android.encrypt.json.copyBytes
import io.novasama.substrate_sdk_android.encrypt.xsalsa20poly1305.SecretBox

private val NONCE_OFFSET = 0
private val NONCE_SIZE = 24

private val DATA_OFFSET = NONCE_OFFSET + NONCE_SIZE

object XSalsa20Poly1305Cryptor : JsonCryptor {

    override fun decrypt(keyGenerationResult: JsonTypeDecoder.KeyGenerationResult): ByteArray? {

        val byteData = keyGenerationResult.encryptedData

        val nonce = byteData.copyBytes(0, NONCE_SIZE)
        val encryptedData = byteData.copyOfRange(DATA_OFFSET, byteData.size)

        val secret = SecretBox(keyGenerationResult.secret).open(nonce, encryptedData)

        // SecretBox returns empty array if key is not correct
        return if (secret.isEmpty()) null else secret
    }

    override fun encrypt(
        keyGenerationResult: JsonTypeEncoder.KeyGenerationResult,
        data: ByteArray
    ): ByteArray {
        val secretBox = SecretBox(keyGenerationResult.encryptionKey)
        val nonce = secretBox.nonce(data)

        val secret = secretBox.seal(nonce, data)

        return keyGenerationResult.encryptingPrefix + nonce + secret
    }
}
