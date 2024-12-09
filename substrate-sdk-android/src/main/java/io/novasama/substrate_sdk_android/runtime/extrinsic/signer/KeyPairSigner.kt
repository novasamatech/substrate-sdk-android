package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.InheritedImplication
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.GeneralTransactionSigner
import io.novasama.substrate_sdk_android.encrypt.Signer as MessageSigner

class KeyPairSigner(
    private val keypair: Keypair,
    private val encryption: MultiChainEncryption
) : Signer, GeneralTransactionSigner {

    override suspend fun signExtrinsic(payloadExtrinsic: SignerPayloadExtrinsic): SignedExtrinsic {
        val messageToSign = payloadExtrinsic.encodedSignaturePayload(hashBigPayloads = true)

        val signatureWrapper = MessageSigner.sign(
            encryption,
            messageToSign,
            keypair,
        )

        return SignedExtrinsic(payloadExtrinsic, signatureWrapper)
    }

    override suspend fun signRaw(payload: SignerPayloadRaw): SignedRaw {
        val signatureWrapper = MessageSigner.sign(
            encryption,
            payload.message,
            keypair,
            payload.skipMessageHashing
        )

        return SignedRaw(payload, signatureWrapper)
    }

    context(TransactionExtension)
    override suspend fun signInheritedImplication(
        inheritedImplication: InheritedImplication,
        signingPayload: ByteArray,
        accountId: AccountId,
    ): SignatureWrapper {
        return MessageSigner.sign(encryption, signingPayload, keypair)
    }
}
