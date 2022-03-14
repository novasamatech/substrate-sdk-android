package jp.co.soramitsu.fearless_utils.encrypt.qr.formats

import jp.co.soramitsu.fearless_utils.encrypt.qr.QrFormat

class AddressQrFormat(
    private val addressValidator: (String) -> Boolean
): QrFormat {

    override fun encode(payload: QrFormat.Payload): String {
        return payload.address
    }

    override fun decode(qrContent: String): QrFormat.Payload {
        return if (addressValidator(qrContent)) {
            QrFormat.Payload(address = qrContent)
        } else {
            throw QrFormat.InvalidFormatException("Supplied address has invalid format")
        }
    }
}