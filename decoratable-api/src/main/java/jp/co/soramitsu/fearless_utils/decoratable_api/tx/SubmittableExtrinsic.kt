package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi

class SubmittableExtrinsic(
    call: GenericCall.Instance,
    private val api: SubstrateApi,
) : GenericCall.Instance by call {

    suspend fun sign(keypair: Keypair): String {
        return TODO()
    }
}
