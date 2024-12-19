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
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtensionValue
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata
import org.bouncycastle.asn1.x509.Extensions

internal abstract class BaseInheritedImplication(
    override val call: GenericCall.Instance,

    override val succeedingExtensions: List<SucceedingExtensionValues>,

    val runtime: RuntimeSnapshot,

    protected val currentNestingLevel: Int,
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
            explicit = explicit,
            nestingLevel = currentNestingLevel
        )

        val newSucceedingExtensionValues = buildList {
            add(nextSucceedingExtensionValues)
            addAll(succeedingExtensions)
        }

        return newSucceedingExtensionValues
    }

    protected fun ScaleCodecWriter.encodeCall() {
        GenericCall.encode(this, runtime, call)
    }

    protected fun ScaleCodecWriter.encodeExtensions() {
        val (fromPreviousLevels, fromCurrentLevel) = partitionByNestingBoundary()

        encodeExtensions(fromPreviousLevels)
        encodeExtensions(fromCurrentLevel)
    }

    private fun ScaleCodecWriter.encodeExtensions(extensions: List<SucceedingExtensionValues>) {
        // Encode explicits
        extensions.onEach { extensionValues ->
            encodeExtensionValue(
                extensionValues.transactionExtension,
                extensionValues.explicit,
                extensionValues.extensionMetadata.includedInExtrinsic
            )
        }

        // Encode implicits
        extensions.onEach { extensionValues ->
            encodeExtensionValue(
                extensionValues.transactionExtension,
                extensionValues.implicit,
                extensionValues.extensionMetadata.includedInSignature
            )
        }
    }

    private fun partitionByNestingBoundary(): Pair<List<SucceedingExtensionValues>, List<SucceedingExtensionValues>> {
        val firstExtensionWithLowerNestedLevel = succeedingExtensions.indexOfFirst { it.nestingLevel < currentNestingLevel }

        val inheritedFromPreviousLevel: List<SucceedingExtensionValues>
        val inheritedFromCurrentLevel: List<SucceedingExtensionValues>

        if (firstExtensionWithLowerNestedLevel == -1) {
            inheritedFromPreviousLevel = emptyList()
            inheritedFromCurrentLevel = succeedingExtensions
        } else {
            inheritedFromPreviousLevel = succeedingExtensions.subList(firstExtensionWithLowerNestedLevel, succeedingExtensions.size)
            inheritedFromCurrentLevel = succeedingExtensions.subList(0, firstExtensionWithLowerNestedLevel)
        }

        return inheritedFromPreviousLevel to inheritedFromCurrentLevel
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
