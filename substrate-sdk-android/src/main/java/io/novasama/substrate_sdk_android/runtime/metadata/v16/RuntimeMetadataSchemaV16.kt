package io.novasama.substrate_sdk_android.runtime.metadata.v16

import io.novasama.substrate_sdk_android.runtime.metadata.StorageEntryModifier
import io.novasama.substrate_sdk_android.runtime.metadata.v14.LookupSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.MapTypeV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.SignedExtensionMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v15.RuntimeApiMethodParamMetadata
import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.scale.byteArray
import io.novasama.substrate_sdk_android.scale.compactInt
import io.novasama.substrate_sdk_android.scale.custom
import io.novasama.substrate_sdk_android.scale.dataType.DataType
import io.novasama.substrate_sdk_android.scale.dataType.keyedUnion
import io.novasama.substrate_sdk_android.scale.dataType.list
import io.novasama.substrate_sdk_android.scale.dataType.noValue
import io.novasama.substrate_sdk_android.scale.dataType.scalable
import io.novasama.substrate_sdk_android.scale.dataType.tuple
import io.novasama.substrate_sdk_android.scale.enum
import io.novasama.substrate_sdk_android.scale.keyedEnum
import io.novasama.substrate_sdk_android.scale.schema
import io.novasama.substrate_sdk_android.scale.sizedByteArray
import io.novasama.substrate_sdk_android.scale.string
import io.novasama.substrate_sdk_android.scale.uint8
import io.novasama.substrate_sdk_android.scale.vector
import io.novasama.substrate_sdk_android.scale.dataType.compactInt as compactIntType
import io.novasama.substrate_sdk_android.scale.dataType.string as stringType
import io.novasama.substrate_sdk_android.scale.dataType.uint8 as uint8Type

/*
 * SCALE schema for FRAME metadata v16.
 *
 * v16 is not a strict superset of v15 on the wire (the top-level `type` compact was dropped, the
 * extrinsic layout changed and every pallet item gained a trailing `deprecation_info`), so it is
 * modelled as a standalone schema tree rather than reusing the `PostV14MetadataSchema` hierarchy.
 * Leaf structs that are byte-compatible with earlier versions are reused directly (lookup, storage
 * map type, transaction/signed extension, runtime-api param).
 *
 * Deprecation information is decoded so that subsequent fields stay byte-aligned, but is not
 * surfaced in the domain model.
 *
 * Reference: https://github.com/paritytech/frame-metadata/blob/main/frame-metadata/src/v16.rs
 */
object RuntimeMetadataSchemaV16 : Schema<RuntimeMetadataSchemaV16>() {
    val lookup by schema(LookupSchema)
    val pallets by vector(PalletMetadataV16)
    val extrinsic by schema(ExtrinsicMetadataV16)
    val apis by vector(RuntimeApiMetadataV16)
    val outerEnums by schema(OuterEnumsV16)
    val custom by schema(CustomMetadataV16)
}

/**
 * `ItemDeprecationInfo` — variants: `NotDeprecated` (0), `DeprecatedWithoutNote` (1),
 * `Deprecated { note, since }` (2).
 */
private fun <S : Schema<S>> S.itemDeprecationInfo() =
    keyedEnum(noValue, noValue, scalable(DeprecationNoteV16))

/**
 * `EnumDeprecationInfo(BTreeMap<u8, VariantDeprecationInfo>)` — encoded as a `Vec<(u8, variant)>`.
 * `VariantDeprecationInfo` uses codec indices 1 (`DeprecatedWithoutNote`) and 2 (`Deprecated`).
 */
private val variantDeprecationInfoType: DataType<Pair<Int, Any?>> = keyedUnion(
    mapOf(
        1 to noValue,
        2 to scalable(DeprecationNoteV16)
    )
)
private val enumDeprecationInfoType: DataType<List<Pair<UByte, Pair<Int, Any?>>>> =
    list(tuple(uint8Type, variantDeprecationInfoType))

object DeprecationNoteV16 : Schema<DeprecationNoteV16>() {
    val note by string()
    val since by string().optional()
}

object ExtrinsicMetadataV16 : Schema<ExtrinsicMetadataV16>() {
    val versions by vector(uint8Type)

    val addressType by compactInt()
    val callType by compactInt()
    val signatureType by compactInt()

    // BTreeMap<u8, Vec<Compact<u32>>>
    val transactionExtensionsByVersion by custom(list(tuple(uint8Type, list(compactIntType))))

    // TransactionExtensionMetadata { identifier, ty, implicit } is byte-compatible with v14 signed extension
    val transactionExtensions by vector(SignedExtensionMetadataV14)
}

object PalletMetadataV16 : Schema<PalletMetadataV16>() {
    val name by string()
    val storage by schema(StorageMetadataV16).optional()
    val calls by schema(PalletCallMetadataV16).optional()
    val events by schema(PalletEventMetadataV16).optional()
    val constants by vector(PalletConstantMetadataV16)
    val errors by schema(PalletErrorMetadataV16).optional()
    val associatedTypes by vector(PalletAssociatedTypeMetadataV16)
    val viewFunctions by vector(PalletViewFunctionMetadataV16)
    val index by uint8()
    val docs by vector(stringType)
    val deprecationInfo by itemDeprecationInfo()
}

object StorageMetadataV16 : Schema<StorageMetadataV16>() {
    val prefix by string()
    val entries by vector(StorageEntryMetadataV16)
}

object StorageEntryMetadataV16 : Schema<StorageEntryMetadataV16>() {
    val name by string()
    val modifier by enum(StorageEntryModifier::class)
    val type by enum(
        compactIntType,
        scalable(MapTypeV14),
    )
    val default by byteArray()
    val documentation by vector(stringType)
    val deprecationInfo by itemDeprecationInfo()
}

object PalletCallMetadataV16 : Schema<PalletCallMetadataV16>() {
    val type by compactInt()
    val deprecationInfo by custom(enumDeprecationInfoType)
}

object PalletEventMetadataV16 : Schema<PalletEventMetadataV16>() {
    val type by compactInt()
    val deprecationInfo by custom(enumDeprecationInfoType)
}

object PalletErrorMetadataV16 : Schema<PalletErrorMetadataV16>() {
    val type by compactInt()
    val deprecationInfo by custom(enumDeprecationInfoType)
}

object PalletConstantMetadataV16 : Schema<PalletConstantMetadataV16>() {
    val name by string()
    val type by compactInt()
    val value by byteArray() // vector<u8>
    val documentation by vector(stringType)
    val deprecationInfo by itemDeprecationInfo()
}

object PalletAssociatedTypeMetadataV16 : Schema<PalletAssociatedTypeMetadataV16>() {
    val name by string()
    val type by compactInt()
    val docs by vector(stringType)
}

object PalletViewFunctionMetadataV16 : Schema<PalletViewFunctionMetadataV16>() {
    val id by sizedByteArray(VIEW_FUNCTION_ID_SIZE)
    val name by string()
    val inputs by vector(RuntimeApiMethodParamMetadata)
    val outputType by compactInt()
    val docs by vector(stringType)
    val deprecationInfo by itemDeprecationInfo()

    const val VIEW_FUNCTION_ID_SIZE = 32
}

object RuntimeApiMetadataV16 : Schema<RuntimeApiMetadataV16>() {
    val name by string()
    val methods by vector(RuntimeApiMethodMetadataV16)
    val docs by vector(stringType)
    val version by compactInt() // Compact<u32>
    val deprecationInfo by itemDeprecationInfo()
}

object RuntimeApiMethodMetadataV16 : Schema<RuntimeApiMethodMetadataV16>() {
    val name by string()
    val inputs by vector(RuntimeApiMethodParamMetadata)
    val outputType by compactInt()
    val docs by vector(stringType)
    val deprecationInfo by itemDeprecationInfo()
}

object OuterEnumsV16 : Schema<OuterEnumsV16>() {
    val callEnumType by compactInt()
    val eventEnumType by compactInt()
    val errorEnumType by compactInt()
}

object CustomValueMetadataV16 : Schema<CustomValueMetadataV16>() {
    val type by compactInt()
    val value by byteArray()
}

object CustomMetadataV16 : Schema<CustomMetadataV16>() {
    // BTreeMap<String, CustomValueMetadata>
    val map by custom(list(tuple(stringType, scalable(CustomValueMetadataV16))))
}
