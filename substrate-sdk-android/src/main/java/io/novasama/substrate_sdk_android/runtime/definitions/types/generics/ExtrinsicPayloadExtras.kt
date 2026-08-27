package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.isEmptyStruct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.isEmptyTuple
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata.Companion.DEFAULT_TRANSACTION_EXTENSION_VERSION
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata

/**
 * @see [TransactionExtensionMetadata.includedInExtrinsic]
 */
object ExtrasIncludedInExtrinsic : ExtrinsicPayloadExtras("ExtrinsicPayloadExtras.ExtrasIncludedInExtrinsic") {

    override fun getTypeFrom(signedExtension: TransactionExtensionMetadata): Type<*>? {
        return signedExtension.includedInExtrinsic
    }
}

/**
 * @see [TransactionExtensionMetadata.includedInSignature]
 */
object ExtrasIncludedInSignature :
    ExtrinsicPayloadExtras("ExtrinsicPayloadExtras.ExtrasIncludedInSignature") {
    override fun getTypeFrom(signedExtension: TransactionExtensionMetadata): Type<*>? {
        return signedExtension.includedInSignature
    }
}

typealias ExtrinsicPayloadExtrasInstance = Map<TransactionExtensionId, Any?>

abstract class ExtrinsicPayloadExtras(name: String) : Type<ExtrinsicPayloadExtrasInstance>(name) {

    companion object {

        fun shouldSkipEncoding(type: RuntimeType<*, *>): Boolean {
            // this is for better backward-compatibility -
            // clients might pass null instead of empty struct / empty tuple that are specified in
            // RuntimeMetadata.signedExtensions v14
            return type.isNullType() || type.isEmptyStruct() || type.isEmptyTuple()
        }
    }

    protected abstract fun getTypeFrom(signedExtension: TransactionExtensionMetadata): Type<*>?

    /**
     * Decodes extras using transaction extensions set from [DEFAULT_TRANSACTION_EXTENSION_VERSION].
     * Prefer version-aware overload when extensions version is known
     */
    override fun decode(
        scaleCodecReader: ScaleCodecReader,
        runtime: RuntimeSnapshot
    ): ExtrinsicPayloadExtrasInstance {
        return decode(scaleCodecReader, runtime, DEFAULT_TRANSACTION_EXTENSION_VERSION.toByte())
    }

    /**
     * Encodes extras using transaction extensions set from [DEFAULT_TRANSACTION_EXTENSION_VERSION].
     * Prefer version-aware overload when extensions version is known
     */
    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: ExtrinsicPayloadExtrasInstance
    ) {
        encode(scaleCodecWriter, runtime, value, DEFAULT_TRANSACTION_EXTENSION_VERSION.toByte())
    }

    fun decode(
        scaleCodecReader: ScaleCodecReader,
        runtime: RuntimeSnapshot,
        extensionsVersion: Byte,
    ): ExtrinsicPayloadExtrasInstance {
        val enabledSignedExtras = runtime.metadata.extrinsic.transactionExtensionsOrThrow(extensionsVersion.toInt())

        return enabledSignedExtras.associateBy(
            keySelector = { it.id },
            valueTransform = { signedExtension ->
                getTypeFrom(signedExtension)?.decode(scaleCodecReader, runtime)
            }
        )
    }

    fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: ExtrinsicPayloadExtrasInstance,
        extensionsVersion: Byte,
    ) {
        val enabledSignedExtras = runtime.metadata.extrinsic.transactionExtensionsOrThrow(extensionsVersion.toInt())

        return enabledSignedExtras.forEach { signedExtension ->
            getTypeFrom(signedExtension)?.let { type ->
                if (!shouldSkipEncoding(type)) {
                    val signedExtensionValue = value[signedExtension.id]
                    type.encodeUnsafe(scaleCodecWriter, runtime, signedExtensionValue)
                }
            }
        }
    }

    override val isFullyResolved: Boolean = true

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Map<*, *> && instance.keys.all { it is String }
    }
}
