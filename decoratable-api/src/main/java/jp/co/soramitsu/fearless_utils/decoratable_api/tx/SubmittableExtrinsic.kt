package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.address
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.identifier
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.author.author
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.author.submitExtrinsic
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances.FeeInfo
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances.payment
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances.queryInfo
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.chain
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getRuntimeVersion
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.accountNextIndex
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.system
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.fee.feeSigner
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.mortality.MortalityConstructor
import jp.co.soramitsu.fearless_utils.encrypt.Keyring
import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.ExtrinsicBuilder

typealias TxHash = String

class SubmittableExtrinsic(
    private val call: GenericCall.Instance,
    private val api: SubstrateApi,
) : GenericCall.Instance by call {

    suspend fun sign(sender: Keypair): String {
        val address = api.options.accountIdentifierConstructor.address(sender)

        val mortality = MortalityConstructor.constructMortality(api)
        val runtimeVersion = api.rpc.chain.getRuntimeVersion()

        return ExtrinsicBuilder(
            runtime = api.chainState.runtime,
            keypair = sender,
            nonce = api.rpc.system.accountNextIndex(address),
            runtimeVersion = runtimeVersion,
            genesisHash = api.chainState.genesisHash().fromHex(),
            multiChainEncryption = MultiChainEncryption.Substrate(sender.encryptionType),
            accountIdentifier = api.options.accountIdentifierConstructor.identifier(sender).forEncoding,
            blockHash = mortality.blockHash.fromHex(),
            era = mortality.era
        )
            .call(call)
            .build(useBatchAll = true)
    }

    suspend fun paymentInfo(
        sender: Keypair = Keyring.feeSigner()
    ): FeeInfo {
        val tx = sign(sender)

        return api.rpc.payment.queryInfo(tx)
    }

    suspend fun signAndSend(
        sender: Keypair
    ): TxHash {
        val tx = sign(sender)

        return api.rpc.author.submitExtrinsic(tx)
    }
}
