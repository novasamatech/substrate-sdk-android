package jp.co.soramitsu.fearless_utils.decoratable_api.rpc

import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.storage.storageChange
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcCall1
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcSubscription1

interface AuthorRpc : DecoratableRPCModule

val DecoratableRPC.author: AuthorRpc
    get() = decorate("author") {
        object : AuthorRpc, DecoratableRPCModule by this {}
    }

val AuthorRpc.submitExtrinsic: RpcCall1<String, String>
    get() = with(decorator) {
        call1("submitExtrinsic", asString)
    }

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
