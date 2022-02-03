package jp.co.soramitsu.fearless_utils.samples.decoratable_api

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.invoke
import jp.co.soramitsu.fearless_utils.encrypt.Keyring
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.balances.balances
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.balances.transfer
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.historyDepth
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.ledger
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.staking
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.types.MultiAddress
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.utility.batch
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.utility.utility
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
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
        socketService.start("wss://westend-rpc.polkadot.io")

        val types = getFileContentFromResources("westend.json")

        val api = SubstrateApi(
            socketService = socketService,
            jsonCodec = jsonCodec,
            typesJsons = listOf(types)
        )

        val account = Keyring.sampleAccount().getOrThrow()
        val accountId = api.options.accountIdentifierConstructor.identifier(account.publicKey).accountId
        val targetAddress = MultiAddress.Id(accountId)

        val feeInfo = api.tx.utility.batch(
            api.tx.balances.transfer(targetAddress, 123.toBigInteger()),
            api.tx.balances.transfer(targetAddress, 123.toBigInteger()),
        )
            .paymentInfo(account)
        println(feeInfo.partialFee)

        val historyDepth = api.query.staking.historyDepth()
        print(historyDepth)

        val ledger = api.query.staking.ledger("5E7C1NtJhfztSa4iKf8qYw1Ps88LbTTnjE65yUcf6Q9FZwqT".toAccountId())
        print(ledger)
    }

    private fun getFileContentFromResources(fileName: String): String {
        return getResourceReader(fileName).readText()
    }

    private fun getResourceReader(fileName: String): Reader {
        val stream = javaClass.classLoader!!.getResourceAsStream(fileName)

        return BufferedReader(InputStreamReader(stream))
    }
}
