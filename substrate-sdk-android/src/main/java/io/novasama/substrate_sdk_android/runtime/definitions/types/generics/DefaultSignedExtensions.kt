package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Option
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.Compact
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.FixedByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata.Companion.onlyInExtrinsic
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata.Companion.onlyInSignature

object DefaultSignedExtensions {

    const val CHECK_MORTALITY = "CheckMortality"
    const val CHECK_NONCE = "CheckNonce"
    const val CHECK_TX_PAYMENT = "ChargeTransactionPayment"
    const val CHECK_GENESIS = "CheckGenesis"
    const val CHECK_SPEC_VERSION = "CheckSpecVersion"
    const val CHECK_TX_VERSION = "CheckTxVersion"
    const val CHECK_METADATA_HASH = "CheckMetadataHash"

    val ALL = listOf(
        SignedExtensionMetadata(
            id = CHECK_MORTALITY,
            includedInExtrinsic = EraType,
            includedInSignature = H256
        ),
        onlyInExtrinsic(CHECK_NONCE, Compact("Compact<Index>")),
        onlyInExtrinsic(CHECK_TX_PAYMENT, Compact("Compact<u32>")),
        onlyInSignature(CHECK_GENESIS, H256),
        onlyInSignature(CHECK_SPEC_VERSION, u32),
        onlyInSignature(CHECK_TX_VERSION, u32),

        // This one should not be included in any pre-v14 runtime
        // which is the use-case for this hard-coded list
        // But we support it anyway just in case
        SignedExtensionMetadata(
            id = CHECK_METADATA_HASH,
            includedInExtrinsic = u8,
            includedInSignature = Option(
                "OptionMetadataHash",
                TypeReference(FixedByteArray("MetadataHash", 32))
            )
        )
    )
}
