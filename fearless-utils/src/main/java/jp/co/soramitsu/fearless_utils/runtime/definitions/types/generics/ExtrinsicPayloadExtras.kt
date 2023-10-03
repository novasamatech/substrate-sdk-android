package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.isEmptyStruct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.isEmptyTuple
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionId
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionMetadata

object SignedExtras : ExtrinsicPayloadExtras("ExtrinsicPayloadExtras.SignedExtras") {

    override fun getTypeFrom(signedExtension: SignedExtensionMetadata): Type<*>? {
        return signedExtension.type
    }
}

object AdditionalSignedExtras :
    ExtrinsicPayloadExtras("ExtrinsicPayloadExtras.AdditionalSignedExtras") {
    override fun getTypeFrom(signedExtension: SignedExtensionMetadata): Type<*>? {
        return signedExtension.additionalSigned
    }
}

typealias ExtrinsicPayloadExtrasInstance = Map<SignedExtensionId, Any?>

abstract class ExtrinsicPayloadExtras(name: String) : Type<ExtrinsicPayloadExtrasInstance>(name) {

    protected abstract fun getTypeFrom(signedExtension: SignedExtensionMetadata): Type<*>?

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

    private fun shouldSkipEncoding(type: RuntimeType<*, *>): Boolean {
        // this is for better backward-compatibility -
        // clients might pass null instead of empty struct / empty tuple that are specified in
        // RuntimeMetadata.signedExtensions v14
        return type.isNullType() || type.isEmptyStruct() || type.isEmptyTuple()
    }
}
