package jp.co.soramitsu.fearless_utils.runtime.metadata.builder

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.DefaultSignedExtensions
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadataReader
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionMetadata

interface RuntimeBuilder {

    fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        knownSignedExtensions: List<SignedExtensionMetadata> = DefaultSignedExtensions.ALL,
    ): RuntimeMetadata
}

object VersionedRuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        knownSignedExtensions: List<SignedExtensionMetadata>,
    ): RuntimeMetadata {
        return when (reader.metadataVersion) {
            14 -> V14RuntimeBuilder.buildMetadata(reader, typeRegistry, knownSignedExtensions)
            else -> V13RuntimeBuilder.buildMetadata(reader, typeRegistry, knownSignedExtensions)
        }
    }
}
