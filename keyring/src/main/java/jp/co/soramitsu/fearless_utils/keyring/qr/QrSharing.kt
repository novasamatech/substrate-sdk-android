package jp.co.soramitsu.fearless_utils.keyring.qr

import jp.co.soramitsu.fearless_utils.extensions.tryFindNonNull

class QrSharing(
    private val decodingFormats: List<QrFormat>,
    private val encodingFormat: QrFormat
) {

    fun encode(payload: QrFormat.Payload): String {
        return encodingFormat.encode(payload)
    }

    fun decode(qrContent: String): QrFormat.Payload {
        return decodingFormats.tryFindNonNull {
            runCatching { it.decode(qrContent) }
                .getOrNull()
        } ?: throw QrFormat.InvalidFormatException("Failed to decode QR code content")
    }
}
