package jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic.signer

import jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.keyring.Signing
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic.multiSignatureName
import jp.co.soramitsu.fearless_utils.signing.MultiSignature
import jp.co.soramitsu.fearless_utils.signing.Signer
import jp.co.soramitsu.fearless_utils.signing.SignerPayloadRaw
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.publicKeyToAccountId

open class KeypairSigner(
    private val keypair: Keypair
) : Signer {

    override fun signRaw(payload: SignerPayloadRaw): MultiSignature {
        require(payload.origin.contentEquals(keypair.publicKey.publicKeyToAccountId()))

        val signatureWrapper = Signing.sign(
            // TODO adopt for ethereum
            multiChainEncryption = MultiChainEncryption.Substrate(keypair.encryptionType),
            message = payload.data,
            keypair = keypair
        )

        return MultiSignature(
            encryptionType = keypair.encryptionType.multiSignatureName,
            signature = signatureWrapper.signature
        )
    }
}
