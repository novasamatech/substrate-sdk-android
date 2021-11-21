package jp.co.soramitsu.fearless_utils.decoratable_api.rpc.author

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPCModule
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asString

interface AuthorRpc : DecoratableRPCModule

val DecoratableRPC.author: AuthorRpc
    get() = decorate("author") {
        object : AuthorRpc, DecoratableRPCModule by this {}
    }

val AuthorRpc.submitExtrinsic: RpcCall1<String, String>
    get() = with(decorator) {
        call1("submitExtrinsic", asString)
    }