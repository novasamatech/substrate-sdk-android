package jp.co.soramitsu.fearless_utils.decoratable_api.tx.signing

import jp.co.soramitsu.fearless_utils.decoratable_api.ApiDependentFactory
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.keyring.Signing
import jp.co.soramitsu.fearless_utils.keyring.adress.EthereumAccountId
import jp.co.soramitsu.fearless_utils.keyring.adress.EthereumAddress
import jp.co.soramitsu.fearless_utils.keyring.adress.SubstrateAccountId
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic.multiSignatureName
import jp.co.soramitsu.fearless_utils.signing.MultiSignature
import jp.co.soramitsu.fearless_utils.signing.Signer
import jp.co.soramitsu.fearless_utils.signing.SignerPayloadRaw
import java.lang.IllegalArgumentException

open class KeypairSigner(
    private val keypair: Keypair,
    private val api: SubstrateApi
) : Signer {

    companion object;

    override suspend fun signRaw(payload: SignerPayloadRaw): MultiSignature {
        val accountId = api.options.addressing.accountId(keypair.publicKey)

        require(payload.origin.value.contentEquals(accountId.value))

        val multiChainEncryption = when(accountId) {
            is SubstrateAccountId -> MultiChainEncryption.Substrate(keypair.encryptionType)
            is EthereumAccountId -> MultiChainEncryption.Ethereum
            else -> throw IllegalArgumentException("Unknown AccountId Type")
        }

        val signatureWrapper = Signing.sign(
            multiChainEncryption = multiChainEncryption,
            message = payload.data,
            keypair = keypair
        )

        return MultiSignature(
            encryptionType = keypair.encryptionType.multiSignatureName,
            signature = signatureWrapper.signature
        )
    }
}

private class KeypairSignerFactory(
    private val keypair: Keypair
): ApiDependentFactory<KeypairSigner> {

    override fun create(api: SubstrateApi): KeypairSigner {
        return KeypairSigner(keypair, api)
    }
}

fun KeypairSigner.Companion.from(keypair: Keypair): ApiDependentFactory<KeypairSigner> {
    return KeypairSignerFactory(keypair)
}
