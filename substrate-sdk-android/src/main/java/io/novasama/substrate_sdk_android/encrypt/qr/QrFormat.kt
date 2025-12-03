package io.novasama.substrate_sdk_android.encrypt.qr

import java.lang.Exception

interface QrFormat<T> {

    class InvalidFormatException(message: String) : Exception(message)

    fun encode(payload: T): String

    fun decode(qrContent: String): T
}

interface PublicQrFormat : QrFormat<PublicQrFormat.Payload> {

    class Payload(
        val address: String,
        val publicKey: ByteArray? = null,
        val name: String? = null
    )
}
