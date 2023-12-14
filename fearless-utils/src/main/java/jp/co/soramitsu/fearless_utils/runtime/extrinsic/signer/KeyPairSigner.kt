package jp.co.soramitsu.fearless_utils.runtime.extrinsic.signer

import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.Signer as MessageSigner

class KeyPairSigner(
    private val keypair: Keypair,
    private val encryption: MultiChainEncryption
) : Signer {

    override suspend fun signExtrinsic(payloadExtrinsic: SignerPayloadExtrinsic): SignedExtrinsic {
        val messageToSign = payloadExtrinsic.encodedSignaturePayload(hashBigPayloads = true)

        val signatureWrapper = MessageSigner.sign(
            encryption,
            messageToSign,
            keypair,
            skipHashing = false
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
}
