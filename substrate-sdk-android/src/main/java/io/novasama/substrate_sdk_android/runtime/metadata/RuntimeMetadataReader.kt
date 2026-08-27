package io.novasama.substrate_sdk_android.runtime.metadata

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.runtime.metadata.v14.LookupSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PostV14MetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RuntimeMetadataSchemaV14
import io.novasama.substrate_sdk_android.runtime.metadata.v15.RuntimeMetadataSchemaV15
import io.novasama.substrate_sdk_android.runtime.metadata.v16.RuntimeMetadataSchemaV16
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.scale.uint32
import io.novasama.substrate_sdk_android.scale.uint8

object Magic : Schema<Magic>() {
    val magicNumber by uint32()
    val runtimeVersion by uint8()
}

@Suppress("UNCHECKED_CAST")
class RuntimeMetadataReader private constructor(
    val metadataVersion: Int,
    val metadata: EncodableStruct<*>
) {

    val metadataPostV14: EncodableStruct<PostV14MetadataSchema<*>>
        get() {
            require(metadata.schema is PostV14MetadataSchema<*>) {
                "Metadata is pre v14"
            }

            return metadata as EncodableStruct<PostV14MetadataSchema<*>>
        }

    val metadataV15: EncodableStruct<RuntimeMetadataSchemaV15>
        get() {
            require(metadata.schema is RuntimeMetadataSchemaV15) {
                "Metadata is not v15"
            }

            return metadata as EncodableStruct<RuntimeMetadataSchemaV15>
        }

    val metadataV16: EncodableStruct<RuntimeMetadataSchemaV16>
        get() {
            require(metadata.schema is RuntimeMetadataSchemaV16) {
                "Metadata is not v16"
            }

            return metadata as EncodableStruct<RuntimeMetadataSchemaV16>
        }

    /**
     * Type lookup section, available in all post-v14 metadata versions (v14, v15, v16).
     * Convenient entry point for feeding the type parser regardless of the concrete metadata version.
     */
    val lookup: EncodableStruct<LookupSchema>
        get() = when (val schema = metadata.schema) {
            is PostV14MetadataSchema<*> -> metadataPostV14[schema.lookup]
            is RuntimeMetadataSchemaV16 -> metadataV16[RuntimeMetadataSchemaV16.lookup]
            else -> error("Metadata is pre v14, type lookup is not available")
        }

    companion object {

        fun read(metadataScale: String): RuntimeMetadataReader {
            return read(metadataScale.fromHex())
        }

        /**
         * Can be used to read Option<OpaqueMetadata>, which is the response of
         * runtime call metadata_versionedMetadata()
         */
        fun readOpaque(opaqueBytes: ByteArray): RuntimeMetadataReader {
            val scaleCoderReader = ScaleCodecReader(opaqueBytes)
            val exists = scaleCoderReader.readBoolean()
            require(exists) {
                "Non existent metadata"
            }
            // Skip opaque vec length
            scaleCoderReader.readCompactInt()

            return read(scaleCoderReader)
        }

        fun read(metadataBytes: ByteArray): RuntimeMetadataReader {
            val scaleCoderReader = ScaleCodecReader(metadataBytes)
            return read(scaleCoderReader)
        }

        private fun read(reader: ScaleCodecReader): RuntimeMetadataReader {
            val runtimeVersion = Magic.read(reader)[Magic.runtimeVersion].toInt()

            val metadata = when {
                runtimeVersion < 14 -> {
                    RuntimeMetadataSchema.read(reader)
                }
                runtimeVersion == 14 -> {
                    RuntimeMetadataSchemaV14.read(reader)
                }
                runtimeVersion == 15 -> {
                    RuntimeMetadataSchemaV15.read(reader)
                }
                else -> {
                    RuntimeMetadataSchemaV16.read(reader)
                }
            }

            return RuntimeMetadataReader(
                metadataVersion = runtimeVersion,
                metadata = metadata
            )
        }
    }
}
