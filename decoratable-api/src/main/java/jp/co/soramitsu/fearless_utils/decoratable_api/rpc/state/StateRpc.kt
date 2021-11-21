package jp.co.soramitsu.fearless_utils.decoratable_api.rpc.state

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPCModule
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcSubscription1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asOptionalString
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.storage.storageChange

interface StateRpc : DecoratableRPCModule

val DecoratableRPC.state: StateRpc
    get() = decorate("state") {
        object : StateRpc, DecoratableRPCModule by this {}
    }

val StateRpc.getStorage: RpcCall1<String, String?>
    get() = with(decorator) {
        call1("getStorage", asOptionalString)
    }

val StateRpc.subscribeStorage: RpcSubscription1<List<String>, List<Pair<String, String?>>>
    get() = with(decorator) {
        subscription1("subscribeStorage") { subscriptionChange ->
            subscriptionChange.storageChange().changes.map { it[0]!! to it[1] }
        }
    }