package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.config.ss58AddressOf
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.chain
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getRuntimeVersion
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.accountNextIndex
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.system
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.mortality.MortalityConstructor
import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.ExtrinsicBuilder

class SubmittableExtrinsic(
    private val call: GenericCall.Instance,
    private val api: SubstrateApi,
) : GenericCall.Instance by call {

    suspend fun sign(keypair: Keypair): String {
        val address = api.options.accountIdentifierConstructor.address(keypair)

        val mortality = MortalityConstructor.constructMortality(api)
        val runtimeVersion = api.rpc.chain.getRuntimeVersion()

        return ExtrinsicBuilder(
            runtime = api.chainState.runtime,
            keypair = keypair,
            nonce = api.rpc.system.accountNextIndex(address),
            runtimeVersion = runtimeVersion,
            genesisHash = api.chainState.genesisHash().fromHex(),
            multiChainEncryption = MultiChainEncryption.Substrate(keypair.encryptionType),
            accountIdentifier = api.options.accountIdentifierConstructor.id(keypair),
            blockHash = mortality.blockHash.fromHex(),
            era = mortality.era
        )
            .call(call)
            .build(useBatchAll = true)
    }
}
