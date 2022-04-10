package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.author.author
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.author.submitExtrinsic
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances.FeeInfo
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances.payment
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances.queryInfo
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.chain
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getRuntimeVersion
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.accountNextIndex
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.system
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.mortality.MortalityConstructor
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.keyring.keypair.PublicKey
import jp.co.soramitsu.fearless_utils.keyring.signing.DefaultSignatureInstanceConstructor
import jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic.signer.FeeSigner
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.ExtrinsicBuilder
import jp.co.soramitsu.fearless_utils.signing.Signer

typealias TxHash = String

class SubmittableExtrinsic(
    private val call: GenericCall.Instance,
    private val api: SubstrateApi,
) : GenericCall.Instance by call {

    suspend fun sign(
        origin: PublicKey,
        signer: Signer = api.options.signer
    ): String {
        val address = api.options.accountIdentifierConstructor.address(origin)
        val accountId = api.options.accountIdentifierConstructor.identifier(origin)

        val mortality = MortalityConstructor.constructMortality(api)
        val runtimeVersion = api.rpc.chain.getRuntimeVersion()

        return ExtrinsicBuilder(
            runtime = api.chainState.runtime,
            nonce = api.rpc.system.accountNextIndex(address),
            runtimeVersion = runtimeVersion,
            genesisHash = api.chainState.genesisHash().fromHex(),
            origin = accountId.accountId,
            blockHash = mortality.blockHash.fromHex(),
            era = mortality.era,
            signer = signer,
            signatureConstructor = DefaultSignatureInstanceConstructor,
        )
            .call(call)
            .build(useBatchAll = true)
    }

    suspend fun paymentInfo(): FeeInfo {
        val signer = FeeSigner()
        val tx = sign(signer.feeKeypair.publicKey, signer)

        return api.rpc.payment.queryInfo(tx)
    }

    suspend fun signAndSend(origin: PublicKey): TxHash {
        val tx = sign(origin)

        return api.rpc.author.submitExtrinsic(tx)
    }
}
