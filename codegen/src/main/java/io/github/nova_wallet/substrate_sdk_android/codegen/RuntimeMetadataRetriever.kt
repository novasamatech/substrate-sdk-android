package io.github.nova_wallet.substrate_sdk_android.codegen

import jp.co.soramitsu.fearless_utils.coroutines_adapter.executeAsync
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import jp.co.soramitsu.fearless_utils.runtime.RuntimeFactory
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.metadata.GetMetadataRequest
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.nonNull
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.string
import java.io.File

class RuntimeMetadataRetriever(
    private val gsonCodec: GsonCodec,
    private val extraTypesFile: File?,
    private val nodeUrl: String,
) {

    suspend fun constructRuntime(): RuntimeSnapshot {
        val socket = SocketService(gsonCodec.gson)
        socket.start(nodeUrl)

        val runtimeFactory = RuntimeFactory(gsonCodec)
        val runtimeMetadata = socket.executeAsync(GetMetadataRequest, mapper = string().nonNull())

        socket.stop()

        val types = listOfNotNull(extraTypesFile?.readText())

        return runtimeFactory.create(runtimeMetadata, types)
    }
}
