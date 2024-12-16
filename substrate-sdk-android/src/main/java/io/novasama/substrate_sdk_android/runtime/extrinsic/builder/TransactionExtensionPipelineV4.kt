package io.novasama.substrate_sdk_android.runtime.extrinsic.builder

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.AddressInstanceConstructor
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.GeneralTransactionParams
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.getOrAbsent
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.SucceedingExtensionValues
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.explicitsMap
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignature
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignatureMode
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata

class TransactionExtensionPipelineV4(
    private val runtime: RuntimeSnapshot,
    private val extrinsicVersion: ExtrinsicVersion.V4
): TransactionBuildingPipeline {

    override suspend fun constructExtrinsicType(
        generalTransactionParams: GeneralTransactionParams
    ): Extrinsic.ExtrinsicType {
        val initial = InheritedImplicationV4(
            call = generalTransactionParams.call,
            succeedingExtensions = emptyList()
        )

        val allRuntimeExtensions = runtime.metadata.extrinsic.signedExtensions

        val finalImplication = allRuntimeExtensions.foldRight(initial) { extensionMetadata, acc ->
            val extension = generalTransactionParams.extensions.getDisablingSignature(extensionMetadata.id)
            val explicit = extension.explicit(acc, extrinsicVersion, runtime)

            acc.add(extension, extensionMetadata, explicit)
        }

        val manualVerifySignature = VerifySignature(extrinsicVersion.verifySignatureMode)
        val v4Signature = manualVerifySignature.v4Signature(finalImplication, extrinsicVersion, runtime)

        return if (v4Signature == null) {
            Extrinsic.ExtrinsicType.Bare
        } else {
            Extrinsic.ExtrinsicType.Signed(
                accountIdentifier = AddressInstanceConstructor.constructInstance(runtime.typeRegistry, v4Signature.accountId),
                signature = v4Signature.signature,
                signedExtras = finalImplication.succeedingExtensions.explicitsMap()
            )
        }
    }

    private fun Map<TransactionExtensionId, TransactionExtension>.getDisablingSignature(id: TransactionExtensionId): TransactionExtension {
        return if (id == VerifySignature.ID) {
            VerifySignature(VerifySignatureMode.Disabled)
        } else {
            getOrAbsent(id)
        }
    }

    private inner class InheritedImplicationV4(
        override val call: GenericCall.Instance,
        override val succeedingExtensions: List<SucceedingExtensionValues>
    ) : BaseInheritedImplication(call, succeedingExtensions, runtime) {

        override fun ScaleCodecWriter.encodeImplication() {
            encodeCall(call)
            encodeExtensions(succeedingExtensions)
        }

        suspend fun add(
            extension: TransactionExtension,
            extensionMetadata: TransactionExtensionMetadata,
            explicit: Any?
        ): InheritedImplicationV4 {
            return InheritedImplicationV4(
                call = call,
                succeedingExtensions = addExtensionValue(extension, extensionMetadata, explicit)
            )
        }
    }
}