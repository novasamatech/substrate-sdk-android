package jp.co.soramitsu.fearless_utils.coroutines_adapter

import jp.co.soramitsu.fearless_utils.runtime.RuntimeFactory
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.metadata.GetMetadataRequest
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.nonNull
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.string

suspend fun RuntimeFactory.create(
    socketService: SocketService,
    typeJsons: List<String> = emptyList()
): RuntimeSnapshot {
    val metadata = socketService.executeAsync(GetMetadataRequest, mapper = string().nonNull())

    return create(
        runtimeMetadata = metadata,
        typeJsons = typeJsons
    )
}