package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.checkMetadataHash

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension

fun CheckMetadataHash(
    mode: CheckMetadataHashMode
): TransactionExtension {

    val implicit: Any?
    val explicit: Any?

    when (mode) {
        CheckMetadataHashMode.Disabled -> {
            implicit = null
            explicit = modeStructOf(enabled = false)
        }

        is CheckMetadataHashMode.Enabled -> {
            implicit = mode.hash
            explicit = modeStructOf(enabled = true)
        }
    }

    return TransactionExtension(
        name = DefaultSignedExtensions.CHECK_METADATA_HASH,
        implicit = implicit,
        explicit = explicit
    )
}

private fun modeStructOf(enabled: Boolean): Struct.Instance {
    val mode = if (enabled) {
        DictEnum.Entry("Enabled", null)
    } else {
        DictEnum.Entry("Disabled", null)
    }

    return Struct.Instance(mapOf("mode" to mode))
}
