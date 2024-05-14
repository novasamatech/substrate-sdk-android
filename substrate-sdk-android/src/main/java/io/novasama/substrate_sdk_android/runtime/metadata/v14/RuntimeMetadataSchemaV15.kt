@file:OptIn(ExperimentalUnsignedTypes::class)

package io.novasama.substrate_sdk_android.runtime.metadata.v14

import io.novasama.substrate_sdk_android.scale.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.string
import io.novasama.substrate_sdk_android.scale.schema
import io.novasama.substrate_sdk_android.scale.string
import io.novasama.substrate_sdk_android.scale.uint8
import io.novasama.substrate_sdk_android.scale.vector


object RuntimeMetadataSchemaV15 : PostV14MetadataSchema<RuntimeMetadataSchemaV15>() {
    override val lookup by schema(LookupSchema)
    override val pallets by vector(PalletMetadataV15)
    override val extrinsic by schema(ExtrinsicMetadataV15)
    val type by compactInt()
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