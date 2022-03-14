package jp.co.soramitsu.fearless_utils.encrypt.qr.formats

import jp.co.soramitsu.fearless_utils.encrypt.qr.QrFormat
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString

private const val PREFIX = "substrate"

const val DELIMITER = ":"

private const val PARTS_WITH_NAME = 4
private const val PARTS_WITHOUT_NAME = 3

class SubstrateQrFormat : QrFormat {

    override fun encode(payload: QrFormat.Payload): String {
        return with(payload) {
            val publicKeyEncoded = publicKey!!.toHexString(withPrefix = true)

            val withoutName = "$PREFIX$DELIMITER$address$DELIMITER$publicKeyEncoded"

            if (name != null) "$withoutName$DELIMITER$name" else withoutName
        }
    }

    override fun decode(qrContent: String): QrFormat.Payload {
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

        return QrFormat.Payload(address, publicKeyEncoded.fromHex(), name)
    }
}
