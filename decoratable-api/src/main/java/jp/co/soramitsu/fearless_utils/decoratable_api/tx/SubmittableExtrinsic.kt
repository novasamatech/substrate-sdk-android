package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.config.ss58AddressOf
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.mortality.MortalityConstructor

class SubmittableExtrinsic(
    call: GenericCall.Instance,
    private val api: SubstrateApi,
) : GenericCall.Instance by call {

    suspend fun sign(keypair: Keypair): String {
        val chainProperties = api.config.chainProperties()
        val address = chainProperties.ss58AddressOf(keypair.publicKey)

        val mortality = MortalityConstructor.constructMortality(api)

       /* val extrinsicBuilder = ExtrinsicBuilder(
            runtime = api.config.runtime,
            keypair = keypair,
            nonce = api.rpc.system.accountNextIndex(address),
            runtimeVersion =,
            genesisHash = api.config.genesisHash().fromHex(),
            multiChainEncryption = MultiChainEncryption.Substrate(keypair.encryptionType),
            accountIdentifier = ,
            blockHash = mortality.blockHash.fromHex(),
            era = mortality.era
        )*/

        TODO()
    }
}
