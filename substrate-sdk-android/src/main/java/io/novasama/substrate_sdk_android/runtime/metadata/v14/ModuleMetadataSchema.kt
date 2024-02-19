package io.novasama.substrate_sdk_android.runtime.metadata.v14

import io.novasama.substrate_sdk_android.runtime.metadata.StorageEntryModifier
import io.novasama.substrate_sdk_android.runtime.metadata.StorageHasher
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

object PalletMetadataV14 : Schema<PalletMetadataV14>() {
    val name by string()
    val storage by schema(StorageMetadataV14).optional()
    val calls by schema(PalletCallMetadataV14).optional()
    val events by schema(PalletEventMetadataV14).optional()
    val constants by vector(PalletConstantMetadataV14)
    val errors by schema(PalletErrorMetadataV14).optional()
    val index by uint8()
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

object ExtrinsicMetadataV14 : Schema<ExtrinsicMetadataV14>() {
    val type by compactInt()
    val version by uint8()
    val signedExtensions by vector(SignedExtensionMetadataV14)
}

object SignedExtensionMetadataV14 : Schema<SignedExtensionMetadataV14>() {
    val identifier by string()
    val type by compactInt()
    val additionalSigned by compactInt()
}
