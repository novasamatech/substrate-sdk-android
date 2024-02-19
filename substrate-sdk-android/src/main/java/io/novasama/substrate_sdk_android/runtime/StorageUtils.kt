package io.novasama.substrate_sdk_android.runtime

import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.hash.Hasher
import io.novasama.substrate_sdk_android.hash.Hasher.blake2b128Concat
import io.novasama.substrate_sdk_android.hash.Hasher.xxHash128
import io.novasama.substrate_sdk_android.hash.hashConcat

typealias HashFunction = (ByteArray) -> ByteArray

enum class IdentifierHasher(val hasher: HashFunction) {
    Blake2b128concat({ it.blake2b128Concat() }),
    TwoX64Concat(Hasher.xxHash64::hashConcat)
}

class Identifier(
    value: ByteArray,
    identifierHasher: IdentifierHasher
) {
    val id = identifierHasher.hasher(value)
}

object StorageUtils {
    fun createStorageKey(
        service: Service<*>,
        identifier: Identifier?
    ): String {
        val moduleNameBytes = service.module.id.toByteArray()
        val serviceNameBytes = service.id.toByteArray()

        var keyBytes = moduleNameBytes.xxHash128() + serviceNameBytes.xxHash128()

        identifier?.let { keyBytes += it.id }

        return keyBytes.toHexString(withPrefix = true)
    }
}
