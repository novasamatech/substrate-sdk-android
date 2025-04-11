package io.novasama.substrate_sdk_android.runtime.extrinsic.builder

import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckGenesis

fun ExtrinsicBuilder.getGenesisHashOrThrow(): ByteArray {
    return getExtensionOrThrow<CheckGenesis>().genesisHash
}

inline fun <reified T : TransactionExtension> ExtrinsicBuilder.getExtensionOrThrow(): T {
    return requireNotNull(getExtension()) {
        "Extension ${T::class.simpleName} was not found"
    }
}
