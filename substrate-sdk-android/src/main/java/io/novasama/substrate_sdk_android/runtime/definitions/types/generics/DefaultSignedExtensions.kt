package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.Compact
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata.Companion.onlyAdditional
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata.Companion.onlySigned

object DefaultSignedExtensions {

    const val CHECK_MORTALITY = "CheckMortality"
    const val CHECK_NONCE = "CheckNonce"
    const val CHECK_TX_PAYMENT = "ChargeTransactionPayment"
    const val CHECK_GENESIS = "CheckGenesis"
    const val CHECK_SPEC_VERSION = "CheckSpecVersion"
    const val CHECK_TX_VERSION = "CheckTxVersion"

    val ALL = listOf(
        SignedExtensionMetadata(CHECK_MORTALITY, type = EraType, additionalSigned = H256),
        onlySigned(CHECK_NONCE, Compact("Compact<Index>")),
        onlySigned(CHECK_TX_PAYMENT, Compact("Compact<u32>")),
        onlyAdditional(CHECK_GENESIS, H256),
        onlyAdditional(CHECK_SPEC_VERSION, u32),
        onlyAdditional(CHECK_TX_VERSION, u32),
    )
}
