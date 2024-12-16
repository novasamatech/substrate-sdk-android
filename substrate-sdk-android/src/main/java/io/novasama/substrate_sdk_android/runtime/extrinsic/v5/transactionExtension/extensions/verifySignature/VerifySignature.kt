package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.hash.Hasher.blake2b256
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.AddressInstanceConstructor
import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.SignatureInstanceConstructor
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.PAYLOAD_HASH_THRESHOLD
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
        extrinsicVersion: ExtrinsicVersion,
        runtimeSnapshot: RuntimeSnapshot,
    ): Any {
        return when (mode) {
            VerifySignatureMode.Disabled -> disabledExplicit()

            is VerifySignatureMode.Enabled -> {
                val signature = mode.signer.signInheritedImplication(
                    inheritedImplication = inheritedImplication,
                    signingPayload = inheritedImplication.signingPayload(extrinsicVersion),
                    accountId = mode.accountId,
                )

                enabled(signature, mode.accountId, runtimeSnapshot)
            }
        }
    }

    internal suspend fun v4Signature(
        inheritedImplication: InheritedImplication,
        extrinsicVersion: ExtrinsicVersion,
        runtimeSnapshot: RuntimeSnapshot,
    ): SignatureInstance? {
        val explicit = explicit(inheritedImplication, extrinsicVersion, runtimeSnapshot)
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

    private fun InheritedImplication.signingPayload(extrinsicVersion: ExtrinsicVersion): ByteArray {
        val encoded = encoded()

        return when (extrinsicVersion) {
            is ExtrinsicVersion.V4 -> if (encoded.size > PAYLOAD_HASH_THRESHOLD) {
                encoded.blake2b256()
            } else {
                encoded
            }

            is ExtrinsicVersion.V5 -> encoded.blake2b256()
        }
    }
}
