package io.novasama.substrate_sdk_android.runtime.extrinsic.builder

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.extrinsic.BatchMode
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.SendableExtrinsic
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.GeneralTransactionParams
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId
import io.novasama.substrate_sdk_android.runtime.metadata.call
import io.novasama.substrate_sdk_android.runtime.metadata.module

class ExtrinsicBuilder(
    val runtime: RuntimeSnapshot,
    private val extrinsicVersion: ExtrinsicVersion,
    private val batchMode: BatchMode,
    private val extensionNesting: TransactionExtensionNesting = FlatTransactionExtensionNesting()
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

        val pipeline = createTransactionBuildingPipeline()

        val extrinsicType = pipeline.constructExtrinsicType(snapshot)
        val extrinsicInstance = Extrinsic.Instance(extrinsicType, snapshot.call)

        return SendableExtrinsic(runtime, extrinsicInstance)
    }

    private fun createTransactionBuildingPipeline(): TransactionBuildingPipeline {
        return when (extrinsicVersion) {
            is ExtrinsicVersion.V4 -> TransactionExtensionPipelineV4(runtime, extrinsicVersion)
            is ExtrinsicVersion.V5 -> TransactionBuildingPipelineV5(runtime, extrinsicVersion, extensionNesting)
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
}
