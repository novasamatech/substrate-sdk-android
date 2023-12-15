package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.DefaultSignedExtensions
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.skipAliases
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionId
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionValue
import jp.co.soramitsu.fearless_utils.runtime.metadata.findSignedExtension
import java.math.BigInteger

fun ExtrinsicBuilder.signedExtra(id: SignedExtensionId, value: Any?) {
    signedExtension(id, SignedExtensionValue(signedExtra = value))
}

fun ExtrinsicBuilder.additionalSigned(id: SignedExtensionId, value: Any?) {
    signedExtension(id, SignedExtensionValue(additionalSigned = value))
}

fun RuntimeSnapshot.encodeNonce(nonce: BigInteger): Any {
    val nonceExtension = metadata.extrinsic
        .findSignedExtension(DefaultSignedExtensions.CHECK_NONCE) ?: return nonce

    val nonceType = nonceExtension.type?.skipAliases()

    return when {
        nonceType is Struct && nonceType.mapping.size == 1 -> {
            val fieldName = nonceType.mapping.keys.single()

            Struct.Instance(mapOf(fieldName to nonce))
        }

        else -> nonce
    }
}