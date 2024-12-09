package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata

interface RuntimeBuilder {

    fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        fallbackSignedExtensions: List<TransactionExtensionMetadata> = DefaultSignedExtensions.ALL,
    ): RuntimeMetadata
}

object VersionedRuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        fallbackSignedExtensions: List<TransactionExtensionMetadata>,
    ): RuntimeMetadata {
        return when (reader.metadataVersion) {
            15 -> V15RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)
            14 -> V14RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)
            else -> V13RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)
        }
    }
}
