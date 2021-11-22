package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.author.author
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.ExtrinsicBuilder

class SubmittableExtrinsic(
    call: GenericCall.Instance,
    private val api: SubstrateApi,
) : GenericCall.Instance by call {

    suspend fun sign(keypair: Keypair): String {
//        val nonce = api.rpc.author.no
//
//        val extrinsicBuilder = ExtrinsicBuilder(
//            runtime = api.runtime,
//            keypair = keypair,
//            nonce =
//        )

        TODO()
    }
}
