package jp.co.soramitsu.fearless_utils.samples.decoratable_api

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.chain
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getFinalizedHead
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getHeader
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.historyDepth
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.staking
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

class StdoutLogger : Logger {
    override fun log(message: String?) {
        println(message)
    }

    override fun log(throwable: Throwable?) {
        throwable?.printStackTrace()
    }
}

class DecoratableApiSample {

    suspend fun run() {
        val gson = Gson()
        val jsonCodec = GsonCodec(gson)

        val socketService = SocketService(gson)
        socketService.start("wss://rpc.polkadot.io")

        val types = getFileContentFromResources("polkadot.json")

        val api = SubstrateApi(
            socketService = socketService,
            jsonCodec = jsonCodec,
            typesJsons = listOf(types)
        )

        val header = api.rpc.chain.getHeader(null)
        println(header)

        api.query.staking.historyDepth.subscribe()
            .onEach { println(it) }
            .collect()
    }

    private fun getFileContentFromResources(fileName: String): String {
        return getResourceReader(fileName).readText()
    }

    private fun getResourceReader(fileName: String): Reader {
        val stream = javaClass.classLoader!!.getResourceAsStream(fileName)

        return BufferedReader(InputStreamReader(stream))
    }
}