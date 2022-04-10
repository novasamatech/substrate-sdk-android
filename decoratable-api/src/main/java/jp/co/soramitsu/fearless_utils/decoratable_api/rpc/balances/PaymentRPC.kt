package jp.co.soramitsu.fearless_utils.decoratable_api.rpc.balances

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPCModule
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall1
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.Bindings.asJson
import java.math.BigInteger

internal interface PaymentRPC : DecoratableRPCModule

internal val DecoratableRPC.payment: PaymentRPC
    get() = decorate("payment") {
        object : PaymentRPC, DecoratableRPCModule by this {}
    }

internal val PaymentRPC.queryInfo: RpcCall1<String, FeeInfo>
    get() = with(decorator) {
        call1("queryInfo", asJson())
    }

class FeeInfo(
    val partialFee: BigInteger
)
