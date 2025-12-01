package io.novasama.substrate_sdk_android.encrypt.qr.formats

import io.novasama.substrate_sdk_android.encrypt.qr.PublicQrFormat
import io.novasama.substrate_sdk_android.encrypt.qr.QrFormat

class AddressQrFormat(
    private val addressValidator: (String) -> Boolean
) : PublicQrFormat {

    override fun encode(payload: PublicQrFormat.Payload): String {
        return payload.address
    }

    override fun decode(qrContent: String): PublicQrFormat.Payload {
        return if (addressValidator(qrContent)) {
            PublicQrFormat.Payload(address = qrContent)
        } else {
            throw QrFormat.InvalidFormatException("Supplied address has invalid format")
        }
    }
}
