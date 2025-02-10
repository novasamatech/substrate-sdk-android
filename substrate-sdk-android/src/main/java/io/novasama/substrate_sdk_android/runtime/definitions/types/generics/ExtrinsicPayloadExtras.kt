package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.isEmptyStruct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.isEmptyTuple
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

    override fun decode(
        scaleCodecReader: ScaleCodecReader,
        runtime: RuntimeSnapshot
    ): ExtrinsicPayloadExtrasInstance {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

        return enabledSignedExtras.associateBy(
            keySelector = { it.id },
            valueTransform = { signedExtension ->
                getTypeFrom(signedExtension)?.decode(scaleCodecReader, runtime)
            }
        )
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: ExtrinsicPayloadExtrasInstance
    ) {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

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
