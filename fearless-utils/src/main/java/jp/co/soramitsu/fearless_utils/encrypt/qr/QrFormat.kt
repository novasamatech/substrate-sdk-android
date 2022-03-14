package jp.co.soramitsu.fearless_utils.encrypt.qr

import java.lang.Exception

interface QrFormat {

    class InvalidFormatException(message: String): Exception(message)

    class Payload(
        val address: String,
        val publicKey: ByteArray? = null,
        val name: String? = null
    )

    fun encode(payload: Payload): String

    fun decode(qrContent: String): Payload
}