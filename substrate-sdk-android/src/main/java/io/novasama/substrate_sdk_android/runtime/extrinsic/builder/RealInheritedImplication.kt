package io.novasama.substrate_sdk_android.runtime.extrinsic.builder

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.ExtrinsicPayloadExtras
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.InheritedImplication
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.SucceedingExtensionValues
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata

internal abstract class BaseInheritedImplication(
    override val call: GenericCall.Instance,

    override val succeedingExtensions: List<SucceedingExtensionValues>,

    val runtime: RuntimeSnapshot
) : InheritedImplication {

    protected abstract fun ScaleCodecWriter.encodeImplication()

    final override fun encoded(): ByteArray {
        return useScaleWriter {
            encodeImplication()
        }
    }

    protected suspend fun addExtensionValue(
        extension: TransactionExtension,
        extensionMetadata: TransactionExtensionMetadata,
        explicit: Any?
    ): List<SucceedingExtensionValues> {
        val nextSucceedingExtensionValues = SucceedingExtensionValues(
            transactionExtension = extension,
            extensionMetadata = extensionMetadata,
            implicit = extension.implicit(),
            explicit = explicit
        )

        val newSucceedingExtensionValues = buildList {
            add(nextSucceedingExtensionValues)
            addAll(succeedingExtensions)
        }

        return newSucceedingExtensionValues
    }

    protected fun ScaleCodecWriter.encodeCall(call: GenericCall.Instance) {
        GenericCall.encode(this, runtime, call)
    }

    protected fun ScaleCodecWriter.encodeExtensions(succeedingExtensions: List<SucceedingExtensionValues>) {
        // Encode explicits
        succeedingExtensions.onEach { extensionValues ->
            encodeExtensionValue(
                extensionValues.transactionExtension,
                extensionValues.explicit,
                extensionValues.extensionMetadata.includedInExtrinsic
            )
        }

        // Encode implicits
        succeedingExtensions.onEach { extensionValues ->
            encodeExtensionValue(
                extensionValues.transactionExtension,
                extensionValues.implicit,
                extensionValues.extensionMetadata.includedInSignature
            )
        }
    }

    private fun ScaleCodecWriter.encodeExtensionValue(
        extension: TransactionExtension,
        extensionValue: Any?,
        type: RuntimeType<*, *>?,
    ) {
        requireNotNull(type) {
            "Cannot resolve type for ${extension.name}"
        }

        if (ExtrinsicPayloadExtras.shouldSkipEncoding(type)) {
            return
        }

        try {
            type.encodeUnsafe(this, runtime, extensionValue)
        } catch (e: EncodeDecodeException) {
            throw Exception("Failed to encode extension ${extension.name}", e)
        }
    }
}
