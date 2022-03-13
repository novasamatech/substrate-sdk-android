package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
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
import jp.co.soramitsu.fearless_utils.keyring.Keyring
import jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.ExtrinsicBuilder

typealias TxHash = String

class SubmittableExtrinsic(
    private val call: GenericCall.Instance,
    private val api: SubstrateApi,
) : GenericCall.Instance by call {

    suspend fun sign(accountId: AccountId): String {
        val address = api.options.accountIdentifierConstructor.address(sender)

        val mortality = MortalityConstructor.constructMortality(api)
        val runtimeVersion = api.rpc.chain.getRuntimeVersion()

        return ExtrinsicBuilder(
            runtime = api.chainState.runtime,
            keypair = sender,
            nonce = api.rpc.system.accountNextIndex(address),
            runtimeVersion = runtimeVersion,
            genesisHash = api.chainState.genesisHash().fromHex(),
            multiChainEncryption = jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption.Substrate(sender.encryptionType),
            accountIdentifier = api.options.accountIdentifierConstructor.identifier(sender).forEncoding,
            blockHash = mortality.blockHash.fromHex(),
            era = mortality.era
        )
            .call(call)
            .build(useBatchAll = true)
    }

    suspend fun paymentInfo(
        sender: jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair = jp.co.soramitsu.fearless_utils.keyring.Keyring.feeSigner()
    ): FeeInfo {
        val tx = sign(sender)

        return api.rpc.payment.queryInfo(tx)
    }

    suspend fun signAndSend(
        sender: jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
    ): TxHash {
        val tx = sign(sender)

        return api.rpc.author.submitExtrinsic(tx)
    }
}
