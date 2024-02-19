package io.novasama.substrate_sdk_android.runtime.metadata

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RuntimeMetadataSchemaV14
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

    companion object {

        @OptIn(ExperimentalUnsignedTypes::class)
        fun read(metadaScale: String): RuntimeMetadataReader {

            val scaleCoderReader = ScaleCodecReader(metadaScale.fromHex())

            val runtimeVersion = Magic.read(scaleCoderReader)[Magic.runtimeVersion].toInt()

            val metadata = when {
                runtimeVersion < 14 -> {
                    RuntimeMetadataSchema.read(scaleCoderReader)
                }
                else -> {
                    RuntimeMetadataSchemaV14.read(scaleCoderReader)
                }
            }

            return RuntimeMetadataReader(
                metadataVersion = runtimeVersion,
                metadata = metadata
            )
        }
    }
}
