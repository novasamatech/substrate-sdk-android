package jp.co.soramitsu.fearless_utils.samples.decoratable_api

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.config.addressOf
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.accountNextIndex
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.system
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.historyDepth
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.staking
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
import kotlinx.coroutines.flow.collect
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

        val socketService = SocketService(gson, StdoutLogger())
        socketService.start("wss://pub.elara.patract.io/polkadot")

        val types = getFileContentFromResources("polkadot.json")

        val api = SubstrateApi(
            socketService = socketService,
            jsonCodec = jsonCodec,
            typesJsons = listOf(types)
        )

        val publicKey = "0x84bdc405d139399bba3ccea5d3de23316c9deeab661f57e2f4d1720cc6649859".fromHex()
        val address = api.config.chainProperties().addressOf(publicKey)

        println(address)

        val nonce = api.rpc.system.accountNextIndex(address)
        println(nonce)

        api.query.staking.historyDepth.subscribe()
//            .onEach { println(it) }
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
