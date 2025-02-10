package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Null
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApi
import java.math.BigInteger

interface WithName {
    val name: String
}

fun <T : WithName> List<T>.groupByName() = associateBy(WithName::name).toMap()

class RuntimeMetadata(
    val metadataVersion: Int,
    val modules: Map<String, Module>,
    val extrinsic: ExtrinsicMetadata,
    // Present in v15+ metadata. null otherwise
    val apis: List<RuntimeApi>?
)

class ExtrinsicMetadata(
    val version: BigInteger,
    val signedExtensions: List<TransactionExtensionMetadata>
)

typealias TransactionExtensionId = String

class SignedExtensionValue(
    val includedInExtrinsic: Any? = null,
    val includedInSignature: Any? = null,
)

class TransactionExtensionMetadata(
    val id: TransactionExtensionId,

    /**
     * Additional information that is included both into extrinsic and signature payload
     * Those values are configurable by the user and can be extracted from signed extrinsic open decoding
     *
     * Examples: tip, mortality, nonce
     */
    val includedInExtrinsic: RuntimeType<*, *>?,

    /**
     * Additional information, that is only included into signature
     * Those values are non-configurable by the user and should always be equal to those used by runtime that verifies the signature
     * They cannot be extracted from the signed extrinsic
     *
     * Examples: genesis hash, runtime version
     */
    val includedInSignature: RuntimeType<*, *>?
) {

    companion object {

        fun onlyInExtrinsic(id: String, includedInExtrinsic: RuntimeType<*, *>): TransactionExtensionMetadata {
            return TransactionExtensionMetadata(id, includedInExtrinsic, Null)
        }

        fun onlyInSignature(
            id: String,
            includedInSignature: RuntimeType<*, *>
        ): TransactionExtensionMetadata {
            return TransactionExtensionMetadata(id, Null, includedInSignature)
        }
    }
}

fun ExtrinsicMetadata.findSignedExtension(id: TransactionExtensionId): TransactionExtensionMetadata? {
    return signedExtensions.find { it.id == id }
}
