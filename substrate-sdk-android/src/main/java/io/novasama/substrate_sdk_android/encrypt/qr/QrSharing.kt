package io.novasama.substrate_sdk_android.encrypt.qr

import io.novasama.substrate_sdk_android.extensions.tryFindNonNull

class QrSharing(
    private val decodingFormats: List<PublicQrFormat>,
    private val encodingFormat: PublicQrFormat
) {

    fun encode(payload: PublicQrFormat.Payload): String {
        return encodingFormat.encode(payload)
    }

    fun decode(qrContent: String): PublicQrFormat.Payload {
        return decodingFormats.tryFindNonNull {
            runCatching { it.decode(qrContent) }
                .getOrNull()
        } ?: throw QrFormat.InvalidFormatException("Failed to decode QR code content")
    }
}
