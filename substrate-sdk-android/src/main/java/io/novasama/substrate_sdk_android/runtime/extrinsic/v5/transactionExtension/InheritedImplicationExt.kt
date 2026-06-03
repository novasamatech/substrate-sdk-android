package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension

import io.novasama.substrate_sdk_android.hash.Hasher.blake2b256
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckGenesis
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignature
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.enabledOrThrow
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray

inline fun <reified T> List<SucceedingExtensionValues>.findExtensionOrThrow(): T {
    val extensionValues = first { it.transactionExtension is T }
    return extensionValues.transactionExtension as T
}

fun InheritedImplication.getAccountIdOrThrow(): AccountId {
    val verifySignature = succeedingExtensions.findExtensionOrThrow<VerifySignature>()
    return verifySignature.mode.enabledOrThrow().accountId
}

fun InheritedImplication.getGenesisHashOrThrow(): ByteArray {
    val checkGenesis = succeedingExtensions.findExtensionOrThrow<CheckGenesis>()
    return checkGenesis.genesisHash
}

private const val PAYLOAD_HASH_THRESHOLD = 256

/**
 * Convert given [InheritedImplication] to the payload [VerifySignature] expects to be signed by the user
 */
fun InheritedImplication.signingPayload(): ByteArray {
    return when (extrinsicVersion) {
        // Legacy V4-signed extrinsics use the envelope SignedPayload `(call, extra, implicit)`, which is
        // version-byte-free. V4 `encoded()` now carries the TxBaseImplication version byte for embedded
        // extensions, so build the envelope payload explicitly from call + extensions to exclude it.
        is ExtrinsicVersion.V4 -> {
            val encoded = encodedCall() + encodedExtensions()

            if (encoded.size > PAYLOAD_HASH_THRESHOLD) {
                encoded.blake2b256()
            } else {
                encoded
            }
        }

        is ExtrinsicVersion.V5 -> encoded().blake2b256()
    }
}

fun InheritedImplication.transientEncodedCallData(): ByteArray {
    val encodedCallData = encodedCall()
    val encodedCallSize = encodedCallData.size.toBigInteger()
    val encodedCallCompact = compactInt.toByteArray(encodedCallSize)

    return encodedCallCompact + encodedCallData
}
