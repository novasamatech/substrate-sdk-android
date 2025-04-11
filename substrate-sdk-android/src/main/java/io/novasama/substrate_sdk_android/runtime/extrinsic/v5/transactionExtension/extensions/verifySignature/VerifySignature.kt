package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.SignatureInstanceConstructor
import io.novasama.substrate_sdk_android.runtime.extrinsic.builder.ExtrinsicBuilder
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.InheritedImplication
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension

class VerifySignature(
    val mode: VerifySignatureMode,
) : TransactionExtension {

    companion object {

        const val ID = "VerifyMultiSignature"

        fun disabledExplicit(): Any {
            return DictEnum.Entry("Disabled", null)
        }

        fun enabled(signer: GeneralTransactionSigner, accountId: AccountId): VerifySignature {
            return VerifySignature(VerifySignatureMode.Enabled(signer, accountId))
        }

        fun disabled(): VerifySignature {
            return VerifySignature(VerifySignatureMode.Disabled)
        }

        fun ExtrinsicBuilder.setVerifySignature(
            signer: GeneralTransactionSigner,
            accountId: AccountId
        ) {
            setTransactionExtension(enabled(signer, accountId))
        }

        internal fun getSignatureFromExplicit(explicit: Any?): SignatureInstance? {
            val asEnum = explicit as DictEnum.Entry<*>

            return when (asEnum.name) {
                "Disabled" -> null
                "Signed" -> {
                    val structValue = asEnum.value as Struct.Instance

                    SignatureInstance(
                        signature = structValue["signature"],
                        accountId = structValue["account"]!!
                    )
                }

                else -> error("Unknown explicit: $explicit")
            }
        }
    }

    override val name: String = ID

    override suspend fun implicit(): Any? {
        return null
    }

    override suspend fun explicit(
        inheritedImplication: InheritedImplication,
        runtimeSnapshot: RuntimeSnapshot,
    ): Any {
        return when (mode) {
            VerifySignatureMode.Disabled -> disabledExplicit()

            is VerifySignatureMode.Enabled -> {
                val signature = mode.signer.signInheritedImplication(
                    inheritedImplication = inheritedImplication,
                    accountId = mode.accountId,
                )

                enabled(signature, mode.accountId, runtimeSnapshot)
            }
        }
    }

    internal suspend fun v4Signature(
        inheritedImplication: InheritedImplication,
        runtimeSnapshot: RuntimeSnapshot,
    ): SignatureInstance? {
        val explicit = explicit(inheritedImplication, runtimeSnapshot)
        return getSignatureFromExplicit(explicit)
    }

    private fun enabled(
        signatureWrapper: SignatureWrapper,
        accountId: AccountId,
        runtimeSnapshot: RuntimeSnapshot,
    ): Any {
        return DictEnum.Entry(
            name = "Signed",
            value = Struct.Instance(
                mapOf(
                    "signature" to SignatureInstanceConstructor.constructInstance(
                        runtimeSnapshot.typeRegistry,
                        signatureWrapper
                    ),
                    "account" to accountId
                )
            )
        )
    }
}
