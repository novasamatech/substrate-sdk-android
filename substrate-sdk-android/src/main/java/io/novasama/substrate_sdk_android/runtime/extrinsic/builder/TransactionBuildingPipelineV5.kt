package io.novasama.substrate_sdk_android.runtime.extrinsic.builder

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.GeneralTransactionParams
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.getOrAbsent
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.SucceedingExtensionValues
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.explicitsMap
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata

class TransactionBuildingPipelineV5(
    private val runtime: RuntimeSnapshot,
    private val extrinsicVersion: ExtrinsicVersion.V5,
    private val nesting: TransactionExtensionNesting,
) : TransactionBuildingPipeline {

    override suspend fun constructExtrinsicType(
        generalTransactionParams: GeneralTransactionParams
    ): Extrinsic.ExtrinsicType {
        val allRuntimeExtensions = runtime.metadata.extrinsic.signedExtensions

        val lastExtension = allRuntimeExtensions.last().id
        val initial = InheritedImplicationV5(
            call = generalTransactionParams.call,
            succeedingExtensions = emptyList(),
            currentNestedLevel = nesting.nestedLevelOf(lastExtension)
        )

        val finalImplication = allRuntimeExtensions.foldRightIndexed(initial) { idx, extensionMetadata, acc ->
            val extension = generalTransactionParams.extensions.getOrAbsent(extensionMetadata.id)
            val explicit = extension.explicit(acc, extrinsicVersion, runtime)

            val nextExtension = allRuntimeExtensions.getOrNull(idx - 1)?.id
            val nextNestingLevel = nextExtension?.let(nesting::nestedLevelOf) ?: 0

            acc.add(extension, extensionMetadata, explicit, nextNestingLevel)
        }

        return Extrinsic.ExtrinsicType.GeneralTransaction(
            extensionsVersion = extrinsicVersion.extensionVersion,
            extensionExplicits = finalImplication.succeedingExtensions.explicitsMap()
        )
    }

    private inner class InheritedImplicationV5(
        override val call: GenericCall.Instance,
        override val succeedingExtensions: List<SucceedingExtensionValues>,
        currentNestedLevel: Int
    ) : BaseInheritedImplication(call, succeedingExtensions, runtime, currentNestedLevel) {

        override fun ScaleCodecWriter.encodeImplication() {
            writeByte(extrinsicVersion.extensionVersion)
            encodeCall()
            encodeExtensions()
        }

        suspend fun add(
            extension: TransactionExtension,
            extensionMetadata: TransactionExtensionMetadata,
            explicit: Any?,
            nextNestingLevel: Int,
        ): InheritedImplicationV5 {
            return InheritedImplicationV5(
                call = call,
                succeedingExtensions = addExtensionValue(extension, extensionMetadata, explicit),
                currentNestedLevel = nextNestingLevel
            )
        }
    }
}
