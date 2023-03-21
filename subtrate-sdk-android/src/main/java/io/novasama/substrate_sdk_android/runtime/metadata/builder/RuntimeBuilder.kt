package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader

interface RuntimeBuilder {

    fun buildMetadata(reader: RuntimeMetadataReader, typeRegistry: TypeRegistry): RuntimeMetadata
}

object VersionedRuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry
    ): RuntimeMetadata {
        return when (reader.metadataVersion) {
            14 -> V14RuntimeBuilder.buildMetadata(reader, typeRegistry)
            else -> V13RuntimeBuilder.buildMetadata(reader, typeRegistry)
        }
    }
}
