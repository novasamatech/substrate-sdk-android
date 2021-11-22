package jp.co.soramitsu.fearless_utils.decoratable_api

import jp.co.soramitsu.fearless_utils.coroutines_adapter.create
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConst
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConstImpl
import jp.co.soramitsu.fearless_utils.decoratable_api.query.DecoratableQuery
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableTx
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableTxImpl
import jp.co.soramitsu.fearless_utils.json.JsonCodec
import jp.co.soramitsu.fearless_utils.runtime.RuntimeFactory
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SubstrateApi {

    val query: DecoratableQuery

    val tx: DecoratableTx

    val rpc: DecoratableRPC

    val const: DecoratableConst
}

fun SubstrateApi(
    runtime: RuntimeSnapshot,
    jsonCodec: JsonCodec,
    socketService: SocketService,
) = object : SubstrateApi {
    override val query: DecoratableQuery = DecoratableQuery(this, runtime)
    override val tx: DecoratableTx = DecoratableTxImpl(this, runtime)
    override val rpc: DecoratableRPC = DecoratableRPC(jsonCodec, socketService)
    override val const: DecoratableConst = DecoratableConstImpl(runtime)
}


suspend fun SubstrateApi(
    socketService: SocketService,
    jsonCodec: JsonCodec,
    typesJsons: List<String>
) = withContext(Dispatchers.Default) {
    val runtimeFactory = RuntimeFactory(jsonCodec)

    val runtime = runtimeFactory.create(socketService, typesJsons)

    SubstrateApi(runtime, jsonCodec, socketService)
}