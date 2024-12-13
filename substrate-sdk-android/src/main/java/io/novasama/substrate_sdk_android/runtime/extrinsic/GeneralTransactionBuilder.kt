package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.ExtrinsicPayloadExtras
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.AddressInstanceConstructor
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.SendableExtrinsic
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.GeneralTransactionParams
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.getOrAbsent
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.InheritedImplication
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.SucceedingExtensionValues
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.SignatureInstance
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignature
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.call
import io.novasama.substrate_sdk_android.runtime.metadata.module
import java.lang.Exception

class ExtrinsicBuilder(
    val runtime: RuntimeSnapshot,
    private val extrinsicVersion: ExtrinsicVersion,
    private val batchMode: BatchMode,
) {

    private val transactionExtensions = mutableMapOf<TransactionExtensionId, TransactionExtension>()

    private val calls = mutableListOf<GenericCall.Instance>()

    fun setTransactionExtension(transactionExtension: TransactionExtension): ExtrinsicBuilder {
        transactionExtensions[transactionExtension.name] = transactionExtension

        return this
    }

    fun call(call: GenericCall.Instance): ExtrinsicBuilder {
        calls.add(call)
        return this
    }

    fun resetCalls(): ExtrinsicBuilder {
        calls.clear()

        return this
    }

    fun getWrappedCall(): GenericCall.Instance {
        return wrapInBatch(calls.toMutableList())
    }

    suspend fun modify(
        modification: suspend (GeneralTransactionParams) -> GeneralTransactionParams
    ): ExtrinsicBuilder {
        val params = paramsSnapshot()
        val modifiedParams = modification(params)
        applySnapshot(modifiedParams)

        return this
    }

    suspend fun buildExtrinsic(): SendableExtrinsic {
        val snapshot = paramsSnapshot()
        val finalImplication = snapshot.constructFinalInheritedImplication()

        val extrinsic = finalImplication.toExtrinsic()

        return SendableExtrinsic(runtime, extrinsic)
    }

    private fun InheritedImplication.toExtrinsic(): Extrinsic.Instance {
        val extrinsicType = constructExtrinsicType()

        return Extrinsic.Instance(extrinsicType, call)
    }

    private fun InheritedImplication.constructExtrinsicType(): Extrinsic.ExtrinsicType {
        return when (extrinsicVersion) {
            ExtrinsicVersion.V4 -> constructSignedType()
            is ExtrinsicVersion.V5 -> constructGeneralType(extrinsicVersion.extensionVersion)
        }
    }

    private fun InheritedImplication.constructGeneralType(
        extensionVersion: Byte
    ): Extrinsic.ExtrinsicType.GeneralTransaction {
        return Extrinsic.ExtrinsicType.GeneralTransaction(
            extensionsVersion = extensionVersion,
            extensionExplicits = succeedingExtensions.mutableExplicitsMap()
        )
    }

    private fun InheritedImplication.constructSignedType(): Extrinsic.ExtrinsicType.Signed {
        val signatureInstance = succeedingExtensions.getSignatureInstance()

        val explicits = succeedingExtensions.mutableExplicitsMap()
        explicits.disableSignature()

        return Extrinsic.ExtrinsicType.Signed(
            accountIdentifier = AddressInstanceConstructor.constructInstance(
                runtime.typeRegistry,
                signatureInstance.account
            ),
            signature = signatureInstance.signature,
            signedExtras = explicits
        )
    }

    private fun List<SucceedingExtensionValues>.getSignatureInstance(): SignatureInstance {
        val signatureExtensionValues = first { it.transactionExtension is VerifySignature }
        return VerifySignature.getSignatureFromExplicit(signatureExtensionValues.explicit)!!
    }

    private fun List<SucceedingExtensionValues>.mutableExplicitsMap(): MutableMap<TransactionExtensionId, Any?> {
        return associateByTo(
            destination = mutableMapOf(),
            keySelector = { it.transactionExtension.name },
            valueTransform = { it.explicit }
        )
    }

    private fun MutableMap<TransactionExtensionId, Any?>.disableSignature() {
        put(VerifySignature.ID, VerifySignature.disabledExplicit())
    }

    private suspend fun GeneralTransactionParams.constructFinalInheritedImplication(): InheritedImplication {
        val initial = RealInheritedImplication(call, succeedingExtensions = emptyList())

        val allRuntimeExtensions = allExtensionsInRuntime(extrinsicVersion)

        return allRuntimeExtensions.foldRight(initial) { extensionMetadata, acc ->
            val extension = transactionExtensions.getOrAbsent(extensionMetadata.id)
            val explicit = extension.explicit(acc, extrinsicVersion, runtime)

            acc.add(extension, extensionMetadata, explicit)
        }
    }

    private fun paramsSnapshot(): GeneralTransactionParams {
        return GeneralTransactionParams(transactionExtensions.toMap(), getWrappedCall())
    }

    private fun applySnapshot(params: GeneralTransactionParams) {
        transactionExtensions.clear()
        transactionExtensions.putAll(params.extensions)

        calls.clear()
        calls.add(params.call)
    }

    private fun wrapInBatch(calls: List<GenericCall.Instance>): GenericCall.Instance {
        if (calls.size == 1) return calls.single()

        val batchModule = runtime.metadata.module("Utility")

        val batchFunctionName = when (batchMode) {
            BatchMode.BATCH -> "batch"
            BatchMode.BATCH_ALL -> "batch_all"
            BatchMode.FORCE_BATCH -> "force_batch"
        }
        val batchFunction = batchModule.call(batchFunctionName)

        return GenericCall.Instance(
            module = batchModule,
            function = batchFunction,
            arguments = mapOf(
                "calls" to calls
            )
        )
    }

    private inner class RealInheritedImplication(
        override val call: GenericCall.Instance,

        override val succeedingExtensions: List<SucceedingExtensionValues>
    ) : InheritedImplication {

        override fun encoded(): ByteArray {
            return useScaleWriter {
                encodeExtensionsVersion()
                encodeCall(call)
                encodeExtensions(succeedingExtensions)
            }
        }

        suspend fun add(
            extension: TransactionExtension,
            extensionMetadata: TransactionExtensionMetadata,
            explicit: Any?
        ): RealInheritedImplication {
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

            return RealInheritedImplication(call, newSucceedingExtensionValues)
        }
    }

    private fun ScaleCodecWriter.encodeExtensionsVersion() {
        when (extrinsicVersion) {
            ExtrinsicVersion.V4 -> {
                // extension version is not present in extrinsic v4
            }

            is ExtrinsicVersion.V5 -> {
                writeByte(extrinsicVersion.extensionVersion)
            }
        }
    }

    private fun ScaleCodecWriter.encodeCall(call: GenericCall.Instance) {
        GenericCall.encode(this, runtime, call)
    }

    private fun allExtensionsInRuntime(extrinsicVersion: ExtrinsicVersion): List<TransactionExtensionMetadata> {
        val base = runtime.metadata.extrinsic.signedExtensions

        return when (extrinsicVersion) {
            ExtrinsicVersion.V4 -> buildList {
                if (!base.hasVerifySignature()) {
                    add(createVerifySignatureExtensionMetadata())
                }
                addAll(base)
            }

            is ExtrinsicVersion.V5 -> base
        }
    }

    private fun List<TransactionExtensionMetadata>.hasVerifySignature(): Boolean {
        return any { it.id == VerifySignature.ID }
    }

    private fun createVerifySignatureExtensionMetadata(): TransactionExtensionMetadata {
        return TransactionExtensionMetadata(
            id = VerifySignature.ID,
            includedInExtrinsic = Extrinsic.signatureType(runtime),
            includedInSignature = null
        )
    }

    private fun ScaleCodecWriter.encodeExtensions(succeedingExtensions: List<SucceedingExtensionValues>) {
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
