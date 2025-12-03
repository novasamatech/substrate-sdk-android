package io.novasama.substrate_sdk_android.encrypt.qr.formats

import io.novasama.substrate_sdk_android.encrypt.qr.PublicQrFormat
import io.novasama.substrate_sdk_android.encrypt.qr.QrFormat
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.extensions.toHexString

private const val PREFIX = "substrate"

private const val DELIMITER = ":"

private const val PARTS_WITH_NAME = 4
private const val PARTS_WITHOUT_NAME = 3

class SubstrateQrFormat : PublicQrFormat {

    override fun encode(payload: PublicQrFormat.Payload): String {
        return with(payload) {
            val publicKeyEncoded = publicKey!!.toHexString(withPrefix = true)

            val withoutName = "$PREFIX$DELIMITER$address$DELIMITER$publicKeyEncoded"

            if (name != null) "$withoutName$DELIMITER$name" else withoutName
        }
    }

    override fun decode(qrContent: String): PublicQrFormat.Payload {
        val parts = qrContent.split(DELIMITER)

        if (parts.size !in PARTS_WITHOUT_NAME..PARTS_WITH_NAME) {
            throw QrFormat.InvalidFormatException("Number of parts (${parts.size} is out of range")
        }

        val (prefix, address, publicKeyEncoded) = parts

        if (prefix != PREFIX) throw QrFormat.InvalidFormatException("Wrong prefix: $prefix")

        val name = if (parts.size == PARTS_WITH_NAME) {
            parts.last()
        } else {
            null
        }

        return PublicQrFormat.Payload(address, publicKeyEncoded.fromHex(), name = name)
    }
}
