@file:OptIn(ExperimentalUnsignedTypes::class)

package io.novasama.substrate_sdk_android.runtime.metadata.v14

import io.novasama.substrate_sdk_android.runtime.metadata.StorageEntryModifier
import io.novasama.substrate_sdk_android.runtime.metadata.StorageHasher
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import io.novasama.substrate_sdk_android.scale.Field
import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.scale.byteArray
import io.novasama.substrate_sdk_android.scale.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.EnumType
import io.novasama.substrate_sdk_android.scale.dataType.scalable
import io.novasama.substrate_sdk_android.scale.enum
import io.novasama.substrate_sdk_android.scale.schema
import io.novasama.substrate_sdk_android.scale.string
import io.novasama.substrate_sdk_android.scale.uint8
import io.novasama.substrate_sdk_android.scale.vector

abstract class PostV14MetadataSchema<S : PostV14MetadataSchema<S>> : Schema<S>() {

    abstract val lookup: Field<EncodableStruct<LookupSchema>>

    abstract val pallets: Field<List<EncodableStruct<PostV14PalletMetadataSchema<*>>>>

    abstract val extrinsic: Field<EncodableStruct<PostV14ExtrinsicMetadataSchema<*>>>
}

abstract class PostV14ExtrinsicMetadataSchema<S : PostV14ExtrinsicMetadataSchema<S>> : Schema<S>() {

    abstract val version: Field<UByte>

    abstract val signedExtensions: Field<List<EncodableStruct<SignedExtensionMetadataV14>>>
}

abstract class PostV14PalletMetadataSchema<S : PostV14PalletMetadataSchema<S>> : Schema<S>() {

    abstract val name: Field<String>

    abstract val storage: Field<EncodableStruct<StorageMetadataV14>?>

    abstract val calls: Field<EncodableStruct<PalletCallMetadataV14>?>

    abstract val events: Field<EncodableStruct<PalletEventMetadataV14>?>

    abstract val constants: Field<List<EncodableStruct<PalletConstantMetadataV14>>>

    abstract val errors: Field<EncodableStruct<PalletErrorMetadataV14>?>

    abstract val index: Field<UByte>
}

object RuntimeMetadataSchemaV14 : PostV14MetadataSchema<RuntimeMetadataSchemaV14>() {
    override val lookup by schema(LookupSchema)
    override val pallets by vector(PalletMetadataV14)
    override val extrinsic by schema(ExtrinsicMetadataV14)
    val type by compactInt()
}

object PalletMetadataV14 : PostV14PalletMetadataSchema<PalletMetadataV14>() {
    override val name by string()
    override val storage by schema(StorageMetadataV14).optional()
    override val calls by schema(PalletCallMetadataV14).optional()
    override val events by schema(PalletEventMetadataV14).optional()
    override val constants by vector(PalletConstantMetadataV14)
    override val errors by schema(PalletErrorMetadataV14).optional()
    override val index by uint8()
}

object StorageMetadataV14 : Schema<StorageMetadataV14>() {
    val prefix by string()
    val entries by vector(StorageEntryMetadataV14)
}

object StorageEntryMetadataV14 : Schema<StorageEntryMetadataV14>() {
    val name by string()
    val modifier by enum(StorageEntryModifier::class)
    val type by enum(
        io.novasama.substrate_sdk_android.scale.dataType.compactInt,
        scalable(MapTypeV14),
    )
    val default by byteArray()
    val documentation by vector(io.novasama.substrate_sdk_android.scale.dataType.string)
}

object MapTypeV14 : Schema<MapTypeV14>() {
    val hashers by vector(EnumType(StorageHasher::class.java))
    val key by compactInt()
    val value by compactInt()
}

object PalletCallMetadataV14 : Schema<PalletCallMetadataV14>() {
    val type by compactInt()
}

object PalletEventMetadataV14 : Schema<PalletEventMetadataV14>() {
    val type by compactInt()
}

object PalletErrorMetadataV14 : Schema<PalletErrorMetadataV14>() {
    val type by compactInt()
}

object PalletConstantMetadataV14 : Schema<PalletConstantMetadataV14>() {
    val name by string()
    val type by compactInt()
    val value by byteArray() // vector<u8>
    val documentation by vector(io.novasama.substrate_sdk_android.scale.dataType.string)
}

object ExtrinsicMetadataV14 : PostV14ExtrinsicMetadataSchema<ExtrinsicMetadataV14>() {
    val type by compactInt()
    override val version by uint8()
    override val signedExtensions by vector(SignedExtensionMetadataV14)
}

object SignedExtensionMetadataV14 : Schema<SignedExtensionMetadataV14>() {
    val identifier by string()
    val type by compactInt()
    val additionalSigned by compactInt()
}
