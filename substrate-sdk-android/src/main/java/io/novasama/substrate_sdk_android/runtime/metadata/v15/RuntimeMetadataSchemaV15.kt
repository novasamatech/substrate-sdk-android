package io.novasama.substrate_sdk_android.runtime.metadata.v15

import io.novasama.substrate_sdk_android.runtime.metadata.v14.LookupSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletCallMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletConstantMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletErrorMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletEventMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PostV14ExtrinsicMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PostV14MetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PostV14PalletMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.SignedExtensionMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.StorageMetadataV14
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import io.novasama.substrate_sdk_android.scale.Field
import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.scale.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.string
import io.novasama.substrate_sdk_android.scale.schema
import io.novasama.substrate_sdk_android.scale.string
import io.novasama.substrate_sdk_android.scale.uint8
import io.novasama.substrate_sdk_android.scale.vector
import java.math.BigInteger

object RuntimeMetadataSchemaV15 : PostV14MetadataSchema<RuntimeMetadataSchemaV15>() {
    override val lookup by schema(LookupSchema)
    override val pallets by vector(PalletMetadataV15)
    override val extrinsic by schema(ExtrinsicMetadataV15)
    val type by compactInt()

    val apis by vector(RuntimeApiMetadataV15)
}

object ExtrinsicMetadataV15 : PostV14ExtrinsicMetadataSchema<ExtrinsicMetadataV15>() {
    override val version by uint8()

    val addressType by compactInt()
    val callType by compactInt()
    val signatureType by compactInt()
    val extraType by compactInt()

    override val signedExtensions by vector(SignedExtensionMetadataV14)
}

object PalletMetadataV15 : PostV14PalletMetadataSchema<PalletMetadataV15>() {
    override val name by string()
    override val storage by schema(StorageMetadataV14).optional()
    override val calls by schema(PalletCallMetadataV14).optional()
    override val events by schema(PalletEventMetadataV14).optional()
    override val constants by vector(PalletConstantMetadataV14)
    override val errors by schema(PalletErrorMetadataV14).optional()
    override val index by uint8()
    val docs by vector(string)
}

object RuntimeApiMetadataV15 : Schema<RuntimeApiMetadataV15>() {
    val name by string()

    val methods by vector(RuntimeApiMethodMetadata)

    val docs by vector(string)
}

object RuntimeApiMethodMetadata: Schema<RuntimeApiMethodMetadata>() {

    val name by string()

    val inputs by vector(RuntimeApiMethodParamMetadata)

    val outputType by compactInt()

    val docs by vector(string)
}

object RuntimeApiMethodParamMetadata : Schema<RuntimeApiMethodParamMetadata>() {
    val name by string()

    val type by compactInt()
}

val EncodableStruct<RuntimeMetadataSchemaV15>.apis: List<EncodableStruct<RuntimeApiMetadataV15>>
    get() = get(RuntimeMetadataSchemaV15.apis)

@get:JvmName("apiName")
val EncodableStruct<RuntimeApiMetadataV15>.name: String
    get() = get(RuntimeApiMetadataV15.name)

val EncodableStruct<RuntimeApiMetadataV15>.methods: List<EncodableStruct<RuntimeApiMethodMetadata>>
    get() = get(RuntimeApiMetadataV15.methods)

@get:JvmName("methodName")
val EncodableStruct<RuntimeApiMethodMetadata>.name: String
    get() = get(RuntimeApiMethodMetadata.name)

val EncodableStruct<RuntimeApiMethodMetadata>.inputs: List<EncodableStruct<RuntimeApiMethodParamMetadata>>
    get() = get(RuntimeApiMethodMetadata.inputs)

val EncodableStruct<RuntimeApiMethodMetadata>.outputType: BigInteger
    get() = get(RuntimeApiMethodMetadata.outputType)

@get:JvmName("paramName")
val EncodableStruct<RuntimeApiMethodParamMetadata>.name: String
    get() = get(RuntimeApiMethodParamMetadata.name)

val EncodableStruct<RuntimeApiMethodParamMetadata>.type: BigInteger
    get() = get(RuntimeApiMethodParamMetadata.type)