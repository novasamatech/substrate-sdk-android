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
    /**
     * All supported extrinsic format versions (e.g. `[4, 5]`). Distinct from the transaction
     * extension version that keys [transactionExtensionsByVersion].
     */
    val versions: List<BigInteger>,
    /**
     * All transaction extensions present across every supported transaction extension version.
     */
    val transactionExtensions: List<TransactionExtensionMetadata>,
    /**
     * Maps each supported transaction extension version to the indices (into [transactionExtensions])
     * of the extensions that are active for that version.
     *
     * Pre-v16 metadata has no separate extension version, so it exposes a single entry whose value
     * lists all extensions.
     */
    val transactionExtensionsByVersion: Map<Int, List<Int>>
) {

    /**
     * Backwards-compatible constructor for metadata versions that expose a single extrinsic version
     * with all transaction extensions active (v13, v14, v15).
     *
     * Pre-v16 metadata has no explicit transaction extension version, so all extensions are placed
     * under the default one ([DEFAULT_TRANSACTION_EXTENSION_VERSION]).
     */
    constructor(
        version: BigInteger,
        signedExtensions: List<TransactionExtensionMetadata>
    ) : this(
        versions = listOf(version),
        transactionExtensions = signedExtensions,
        transactionExtensionsByVersion = mapOf(
            DEFAULT_TRANSACTION_EXTENSION_VERSION to signedExtensions.indices.toList()
        )
    )

    /**
     * Newest supported extrinsic format version.
     */
    val latestVersion: BigInteger
        get() = versions.maxOrNull() ?: BigInteger.ZERO

    @Deprecated(
        message = "Extrinsic format is now multi-versioned. Use latestVersion or versions.",
        replaceWith = ReplaceWith("latestVersion")
    )
    val version: BigInteger
        get() = latestVersion

    /**
     * Transaction extensions active for the given [extensionsVersion] (the transaction extension
     * version, not the extrinsic format [versions]). Returns an empty list if it is not supported.
     */
    fun transactionExtensions(extensionsVersion: Int): List<TransactionExtensionMetadata> {
        val activeIndices = transactionExtensionsByVersion[extensionsVersion].orEmpty()

        return activeIndices.map(transactionExtensions::get)
    }

    /**
     * Transaction extensions active for the newest supported transaction extension version.
     */
    val latestTransactionExtensions: List<TransactionExtensionMetadata>
        get() {
            val latestExtensionsVersion = transactionExtensionsByVersion.keys.maxOrNull() ?: return emptyList()

            return transactionExtensions(latestExtensionsVersion)
        }

    @Deprecated(
        message = "Transaction extensions are now versioned. Use latestTransactionExtensions or transactionExtensions(version).",
        replaceWith = ReplaceWith("latestTransactionExtensions")
    )
    val signedExtensions: List<TransactionExtensionMetadata>
        get() = latestTransactionExtensions

    companion object {

        /**
         * Transaction extension version used when the metadata does not specify one (pre-v16).
         */
        const val DEFAULT_TRANSACTION_EXTENSION_VERSION = 0
    }
}

typealias TransactionExtensionId = String

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
    return latestTransactionExtensions.find { it.id == id }
}
