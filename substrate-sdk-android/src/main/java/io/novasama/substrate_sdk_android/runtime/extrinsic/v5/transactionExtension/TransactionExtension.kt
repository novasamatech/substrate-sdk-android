package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Era
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckGenesis
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckMortality
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckNonce
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.FixedValueTransactionExtension
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignature
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId

/**
 * Interfaces that describes an arbitrary transaction extension.
 *
 * Transaction extension is an additional piece of functionality that can extend the transaction validation and execution pipeline
 * From the client perspective, each transaction might have up to two fields:
 *
 * 1. Implicits - a piece of data that is not explicitly present in transaction blob but still rather only participate in
 * creation of [InheritedImplication]. InheritedImplication is used by certain extensions to verify signatures and consists of the
 * all transaction information concatenated. @see [InheritedImplication] docs for more info
 * The basic idea behind implicits is to reduce transaction size for values that are known beforehand for the chain
 * For example, its own genesis hash or runtime version. Implicits are those pieces of transaction that
 * are configured by the application or sdk, they are not something user can meaningfully control
 *
 * 2. Explicits - a piece of data that should explicitly present in transaction blob.
 * They also participate in construction of [InheritedImplication].
 * Explicits are those additional pieces of data that are configured and can be provided by the user
 *
 * Here a few examples:
 *
 * [CheckNonce] allows chain to order multiple transactions coming from the same signed origin (account)
 * Its has explicit with type "nonce: Number" and has no implicit
 *
 * [VerifySignature] allows chain to authorize signed origins by verifying signature with the encoded [InheritedImplication]
 * Its has explicit with type "signature: Signature" and has no implicit
 *
 * [CheckGenesis] allows chain to prevent replay attack between multiple substrate chains by validating
 * chain id a.k.a genesis hash.
 * It has no explicit and implicit of type "ByteArray". Chain id is used as implicit since each chain knows
 * its own chain id and there is no need to specify it in advance
 *
 * [CheckMortality] allows to specify transaction validity duration. It has explicit value of type [Era]
 * and implicit value of "BlockHash". Era determines the lifespan whereas block-hash determines the starting point
 */
interface TransactionExtension {

    val name: String

    suspend fun implicit(): Any?

    suspend fun explicit(
        inheritedImplication: InheritedImplication,
        extrinsicVersion: ExtrinsicVersion,
        runtimeSnapshot: RuntimeSnapshot,
    ): Any?
}

fun TransactionExtension(
    name: String,
    implicit: Any?,
    explicit: Any?
): TransactionExtension {
    return FixedValueTransactionExtension(name = name, implicit = implicit, explicit = explicit)
}

fun AbsentExtension(name: String) = TransactionExtension(name, null, null)

fun List<SucceedingExtensionValues>.mutableExplicitsMap(): MutableMap<TransactionExtensionId, Any?> {
    return associateByTo(
        destination = mutableMapOf(),
        keySelector = { it.transactionExtension.name },
        valueTransform = { it.explicit }
    )
}

fun List<SucceedingExtensionValues>.explicitsMap(): Map<TransactionExtensionId, Any?> {
    return mutableExplicitsMap()
}
