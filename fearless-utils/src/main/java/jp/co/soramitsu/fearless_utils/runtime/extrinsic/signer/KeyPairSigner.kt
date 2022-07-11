package jp.co.soramitsu.fearless_utils.runtime.extrinsic.signer

import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.encrypt.SignatureWrapper
import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.Signer as MessageSigner

class KeyPairSigner(
    private val keypair: Keypair,
    private val encryption: MultiChainEncryption
) : Signer {

    override suspend fun signRaw(payload: SignerPayloadRaw): SignatureWrapper {
        return MessageSigner.sign(encryption, payload.message, keypair)
    }
}
