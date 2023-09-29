package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Null
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
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

class SignedExtensionMetadata(
    val id: SignedExtensionId,
    val type: RuntimeType<*, *>?,
    val additionalSigned: RuntimeType<*, *>?
) {

    companion object {

        fun onlySigned(id: String, type: RuntimeType<*, *>): SignedExtensionMetadata {
            return SignedExtensionMetadata(id, type, Null)
        }

        fun onlyAdditional(id: String, additionalSigned: RuntimeType<*, *>): SignedExtensionMetadata {
            return SignedExtensionMetadata(id, Null, additionalSigned)
        }
    }
}

fun ExtrinsicMetadata.findSignedExtension(id: SignedExtensionId): SignedExtensionMetadata? {
    return signedExtensions.find { it.id == id }
}
