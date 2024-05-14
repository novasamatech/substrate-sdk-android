package io.novasama.substrate_sdk_android.runtime.metadata

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.runtime.metadata.v14.LookupSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PostV14MetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RuntimeMetadataSchemaV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RuntimeMetadataSchemaV15
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.scale.uint32
import io.novasama.substrate_sdk_android.scale.uint8

object Magic : Schema<Magic>() {
    val magicNumber by uint32()
    val runtimeVersion by uint8()
}

class RuntimeMetadataReader private constructor(
    val metadataVersion: Int,
    val metadata: EncodableStruct<*>
) {

    @Suppress("UNCHECKED_CAST")
    val metadataPostV14: EncodableStruct<PostV14MetadataSchema<*>>
        get() {
            require(metadata.schema is PostV14MetadataSchema<*>) {
                "Metadata is pre v14"
            }

            return metadata as EncodableStruct<PostV14MetadataSchema<*>>
        }

    companion object {

        fun read(metadaScale: String): RuntimeMetadataReader {
            return read(metadaScale.fromHex())
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

        @OptIn(ExperimentalUnsignedTypes::class)
        private fun read(reader: ScaleCodecReader): RuntimeMetadataReader {
            val runtimeVersion = Magic.read(reader)[Magic.runtimeVersion].toInt()

            val metadata = when{
                runtimeVersion < 14 -> {
                    RuntimeMetadataSchema.read(reader)
                }
                runtimeVersion == 14 -> {
                    RuntimeMetadataSchemaV14.read(reader)
                }
                else -> {
                    RuntimeMetadataSchemaV15.read(reader)
                }
            }

            return RuntimeMetadataReader(
                metadataVersion = runtimeVersion,
                metadata = metadata
            )
        }
    }
}
