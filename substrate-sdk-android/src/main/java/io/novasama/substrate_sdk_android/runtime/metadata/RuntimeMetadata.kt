package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Null
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import java.math.BigInteger

interface WithName {
    val name: String
}

fun <T : WithName> List<T>.groupByName() = associateBy(WithName::name).toMap()

class RuntimeMetadata(
    val runtimeVersion: BigInteger,
    val modules: Map<String, Module>,
    val extrinsic: ExtrinsicMetadata
)

class ExtrinsicMetadata(
    val version: BigInteger,
    val signedExtensions: List<SignedExtensionMetadata>
)

typealias SignedExtensionId = String

class SignedExtensionValue(
    val signedExtra: Any? = null,
    val additionalSigned: Any? = null
)

class SignedExtensionMetadata(
    val id: SignedExtensionId,
    val type: RuntimeType<*, *>?,
    val additionalSigned: RuntimeType<*, *>?
) {

    companion object {

        /**
         * SignedExtras is signature params that are both signed
         * and put separately in payload for verification
         * Examples: tip, mortality
         */
        fun onlySigned(id: String, type: RuntimeType<*, *>): SignedExtensionMetadata {
            return SignedExtensionMetadata(id, type, Null)
        }

        /**
         * AdditionalSigned is signature params that are signed
         * and that are verified by runtime based on-chain state
         * Examples: genesis hash, runtime version
         */
        fun onlyAdditional(id: String, additionalSigned: RuntimeType<*, *>): SignedExtensionMetadata {
            return SignedExtensionMetadata(id, Null, additionalSigned)
        }
    }
}

fun ExtrinsicMetadata.findSignedExtension(id: SignedExtensionId): SignedExtensionMetadata? {
    return signedExtensions.find { it.id == id }
}
