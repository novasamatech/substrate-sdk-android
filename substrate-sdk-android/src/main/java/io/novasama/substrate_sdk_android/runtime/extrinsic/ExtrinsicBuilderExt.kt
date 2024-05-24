package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliases
import io.novasama.substrate_sdk_android.runtime.metadata.findSignedExtension
import java.math.BigInteger

fun RuntimeSnapshot.encodeNonce(nonce: BigInteger): Any {
    val nonceExtension = metadata.extrinsic
        .findSignedExtension(DefaultSignedExtensions.CHECK_NONCE) ?: return nonce

    val nonceType = nonceExtension.includedInExtrinsic?.skipAliases()

    return when {
        nonceType is Struct && nonceType.mapping.size == 1 -> {
            val fieldName = nonceType.mapping.keys.single()

            Struct.Instance(mapOf(fieldName to nonce))
        }

        else -> nonce
    }
}
