package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata

interface InheritedImplication {

    val call: GenericCall.Instance

    val succeedingExtensions: List<SucceedingExtensionValues>

    fun encoded(): ByteArray
}

class SucceedingExtensionValues(
    val transactionExtension: TransactionExtension,
    val extensionMetadata: TransactionExtensionMetadata,
    val implicit: Any?,
    val explicit: Any?
)