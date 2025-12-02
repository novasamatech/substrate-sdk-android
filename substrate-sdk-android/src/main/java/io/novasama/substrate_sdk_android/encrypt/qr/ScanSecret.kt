package io.novasama.substrate_sdk_android.encrypt.qr

sealed class ScanSecret(val data: ByteArray) {

    class Seed(data: ByteArray) : ScanSecret(data)

    class RawKey(data: ByteArray) : ScanSecret(data)
}
