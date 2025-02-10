package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliases
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.Nonce
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.InheritedImplication
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.metadata.findSignedExtension
import java.math.BigInteger

class CheckNonce(
    val nonce: Nonce,
) : TransactionExtension {

    override val name: String = DefaultSignedExtensions.CHECK_NONCE

    override suspend fun implicit(): Any? {
        return null
    }

    override suspend fun explicit(
        inheritedImplication: InheritedImplication,
        extrinsicVersion: ExtrinsicVersion,
        runtimeSnapshot: RuntimeSnapshot
    ): Any? {
        return runtimeSnapshot.encodeNonce(nonce.nonce)
    }

    private fun RuntimeSnapshot.encodeNonce(nonce: BigInteger): Any {
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
}
