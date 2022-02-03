package jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPCModule
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asJson
import java.math.BigInteger

interface PaymentRPC : DecoratableRPCModule

val DecoratableRPC.payment: PaymentRPC
    get() = decorate("payment") {
        object : PaymentRPC, DecoratableRPCModule by this {}
    }

val PaymentRPC.queryInfo: RpcCall1<String, FeeInfo>
    get() = with(decorator) {
        call1("queryInfo", asJson())
    }

class FeeInfo(
    val partialFee: BigInteger
)
