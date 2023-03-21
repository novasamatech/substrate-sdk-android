package io.novasama.substrate_sdk_android.runtime.metadata.v14

import io.novasama.substrate_sdk_android.scale.EncodableStruct
import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.scale.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.EnumType
import io.novasama.substrate_sdk_android.scale.dataType.list
import io.novasama.substrate_sdk_android.scale.dataType.scalable
import io.novasama.substrate_sdk_android.scale.enum
import io.novasama.substrate_sdk_android.scale.schema
import io.novasama.substrate_sdk_android.scale.string
import io.novasama.substrate_sdk_android.scale.uint32
import io.novasama.substrate_sdk_android.scale.uint8
import io.novasama.substrate_sdk_android.scale.vector

object RuntimeMetadataSchemaV14 : Schema<RuntimeMetadataSchemaV14>() {
    val lookup by schema(LookupSchema)
    val pallets by vector(PalletMetadataV14)
    val extrinsic by schema(ExtrinsicMetadataV14)
    val type by compactInt()
}

object LookupSchema : Schema<LookupSchema>() {
    val types by vector(PortableType)
}

object PortableType : Schema<PortableType>() {
    val id by compactInt()
    val type by schema(RegistryType)
}

val EncodableStruct<PortableType>.id
    get() = get(PortableType.id)

val EncodableStruct<PortableType>.type
    get() = get(PortableType.type)

object RegistryType : Schema<RegistryType>() {
    val path by vector(io.novasama.substrate_sdk_android.scale.dataType.string)
    val params by vector(TypeParameter)
    val def by enum(
        scalable(TypeDefComposite),
        scalable(TypeDefVariant),
        scalable(TypeDefSequence),
        scalable(TypeDefArray),
        list(io.novasama.substrate_sdk_android.scale.dataType.compactInt),
        EnumType(TypeDefEnum::class.java),
        scalable(TypeDefCompact),
        scalable(TypeDefBitSequence),
    )
    val docs by vector(io.novasama.substrate_sdk_android.scale.dataType.string)
}

val EncodableStruct<RegistryType>.path
    get() = get(RegistryType.path)

val EncodableStruct<RegistryType>.def
    get() = get(RegistryType.def)

val EncodableStruct<RegistryType>.params
    get() = get(RegistryType.params)

enum class TypeDefEnum(val localName: String) {
    bool("bool"),
    char(""),
    str(""),
    u8("u8"),
    u16("u16"),
    u32("u32"),
    u64("u64"),
    u128("u128"),
    u256("u256"),
    i8("i8"),
    i16("i16"),
    i32("i32"),
    i64("i64"),
    i128("i128"),
    i256("i256")
}

object TypeDefBitSequence : Schema<TypeDefBitSequence>() {
    val bit_store_type by compactInt()
    val bit_order_type by compactInt()
}

object TypeDefCompact : Schema<TypeDefCompact>() {
    val type by compactInt()
}

object TypeDefArray : Schema<TypeDefArray>() {
    val len by uint32()
    val type by compactInt()
}

object TypeDefSequence : Schema<TypeDefSequence>() {
    val type by compactInt()
}

object TypeDefVariant : Schema<TypeDefVariant>() {
    val variants by vector(TypeDefVariantItem)
}

object TypeDefVariantItem : Schema<TypeDefVariantItem>() {
    val name by string()
    val fields2 by vector(TypeDefCompositeField)
    val index by uint8()
    val docs by vector(io.novasama.substrate_sdk_android.scale.dataType.string)
}

object TypeDefComposite : Schema<TypeDefComposite>() {
    val fields2 by vector(TypeDefCompositeField)
}

val EncodableStruct<TypeDefComposite>.fields
    get() = get(TypeDefComposite.fields2)

val EncodableStruct<TypeDefCompositeField>.type
    get() = get(TypeDefCompositeField.type)

val EncodableStruct<TypeDefCompositeField>.name
    get() = get(TypeDefCompositeField.name)

object TypeDefCompositeField : Schema<TypeDefCompositeField>() {
    val name by string().optional()
    val type by compactInt()
    val typeName by string().optional()
    val docs by vector(io.novasama.substrate_sdk_android.scale.dataType.string)
}

object TypeParameter : Schema<TypeParameter>() {
    val name by string()
    val type by compactInt().optional()
}
