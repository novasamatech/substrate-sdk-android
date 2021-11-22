package jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPCModule
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall0
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcSubscription1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asJson
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asOptionalString
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.storage.storageChange

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
