package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.Signer as MessageSigner

class KeyPairSigner(
    private val keypair: Keypair,
    private val encryption: MultiChainEncryption
) : Signer {

    override suspend fun signExtrinsic(payloadExtrinsic: SignerPayloadExtrinsic): SignatureWrapper {
        val messageToSign = payloadExtrinsic.encodedSignaturePayload(hashBigPayloads = true)

        return MessageSigner.sign(encryption, messageToSign, keypair, skipHashing = false)
    }

    override suspend fun signRaw(payload: SignerPayloadRaw): SignatureWrapper {
        return MessageSigner.sign(encryption, payload.message, keypair, payload.skipMessageHashing)
    }
}
