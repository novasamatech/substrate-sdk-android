package jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPCModule
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall0
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asJson
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asNumber
import java.math.BigInteger

interface SystemRpc : DecoratableRPCModule

class ChainProperties(
    val ss58Format: Short?,
    val tokenDecimals: Int,
    val tokenSymbol: String?
)

val DecoratableRPC.system: SystemRpc
    get() = decorate("system") {
        object : SystemRpc, DecoratableRPCModule by this {}
    }

val SystemRpc.properties: RpcCall0<ChainProperties>
    get() = with(decorator) {
        call0("properties", asJson())
    }

val SystemRpc.accountNextIndex: RpcCall1<String, BigInteger>
    get() = with(decorator) {
        call1("accountNextIndex", asNumber)
    }
