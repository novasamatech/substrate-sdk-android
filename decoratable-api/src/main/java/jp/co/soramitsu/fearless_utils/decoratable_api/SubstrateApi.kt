package jp.co.soramitsu.fearless_utils.decoratable_api

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.decoratable_api.query.DecoratableQuery
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableTx

interface SubstrateApi {

    val query: DecoratableQuery

    val tx: DecoratableTx

    val rpc: DecoratableRPC
}

fun SubstrateApi(
    runtime: RuntimeSnapshot,
    socketService: SocketService,
) = object : SubstrateApi {
    override val query: DecoratableQuery = DecoratableQuery(this, runtime)
    override val tx: DecoratableTx = DecoratableTx(this, runtime)
    override val rpc: DecoratableRPC = DecoratableRPC(socketService)
}
