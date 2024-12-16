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
    private val extrinsicVersion: ExtrinsicVersion.V5
) : TransactionBuildingPipeline {

    override suspend fun constructExtrinsicType(
        generalTransactionParams: GeneralTransactionParams
    ): Extrinsic.ExtrinsicType {
        val initial = InheritedImplicationV5(
            call = generalTransactionParams.call,
            succeedingExtensions = emptyList()
        )

        val allRuntimeExtensions = runtime.metadata.extrinsic.signedExtensions

        val finalImplication = allRuntimeExtensions.foldRight(initial) { extensionMetadata, acc ->
            val extension = generalTransactionParams.extensions.getOrAbsent(extensionMetadata.id)
            val explicit = extension.explicit(acc, extrinsicVersion, runtime)

            acc.add(extension, extensionMetadata, explicit)
        }

        return Extrinsic.ExtrinsicType.GeneralTransaction(
            extensionsVersion = extrinsicVersion.extensionVersion,
            extensionExplicits = finalImplication.succeedingExtensions.explicitsMap()
        )
    }

    private inner class InheritedImplicationV5(
        override val call: GenericCall.Instance,
        override val succeedingExtensions: List<SucceedingExtensionValues>
    ) : BaseInheritedImplication(call, succeedingExtensions, runtime) {

        override fun ScaleCodecWriter.encodeImplication() {
            writeByte(extrinsicVersion.extensionVersion)
            encodeCall(call)
            encodeExtensions(succeedingExtensions)
        }

        suspend fun add(
            extension: TransactionExtension,
            extensionMetadata: TransactionExtensionMetadata,
            explicit: Any?
        ): InheritedImplicationV5 {
            return InheritedImplicationV5(
                call = call,
                succeedingExtensions = addExtensionValue(extension, extensionMetadata, explicit)
            )
        }
    }
}