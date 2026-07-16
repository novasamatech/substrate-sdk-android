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
        // Newer-than-15 metadata is parsed with the v16 schema by RuntimeMetadataReader, so build it as v16.
        return when {
            reader.metadataVersion >= 16 -> V16RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)
            reader.metadataVersion == 15 -> V15RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)
            reader.metadataVersion == 14 -> V14RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)
            else -> V13RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)
        }
    }
}
