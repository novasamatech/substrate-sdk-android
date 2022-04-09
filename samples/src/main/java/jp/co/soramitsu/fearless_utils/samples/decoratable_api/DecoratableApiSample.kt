package jp.co.soramitsu.fearless_utils.samples.decoratable_api

import balances.balances
import balances.transfer
import com.google.gson.Gson
import crowdloan.contribute
import crowdloan.crowdloan
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.options.Options
import jp.co.soramitsu.fearless_utils.decoratable_api.options.Substrate
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.invoke
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import jp.co.soramitsu.fearless_utils.keyring.Keyring
import jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic.signer.KeypairSigner
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
import sp_runtime.multiaddress.MultiAddress
import staking.chill
import staking.historyDepth
import staking.ledger
import staking.maxNominations
import staking.setController
import staking.staking
import utility.batch
import utility.utility
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.math.BigInteger

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

        val account = Keyring.sampleAccount().getOrThrow()

        val api = SubstrateApi(
            socketService = socketService,
            jsonCodec = jsonCodec,
            typesJsons = listOf(types),
            options = Options.Substrate(KeypairSigner(account))
        )

        val accountId = api.options.accountIdentifierConstructor.identifier(account.publicKey).accountId
        val targetAddress = MultiAddress.Id(accountId)

        val feeInfo = api.tx.utility.batch(
            api.tx.balances.transfer(targetAddress, 123.toBigInteger()),
            api.tx.staking.setController(targetAddress),
            api.tx.crowdloan.contribute(index = BigInteger.ZERO, value = BigInteger.TEN, signature = null)
        )
            .paymentInfo()
        println(feeInfo.partialFee)

        val historyDepth = api.query.staking.historyDepth()
        println(historyDepth)

        val ledger = api.query.staking.ledger("5E7C1NtJhfztSa4iKf8qYw1Ps88LbTTnjE65yUcf6Q9FZwqT".toAccountId())
        println(ledger?.claimedRewards)

        println(api.const.staking.maxNominations())
    }

    private fun getFileContentFromResources(fileName: String): String {
        return getResourceReader(fileName).readText()
    }

    private fun getResourceReader(fileName: String): Reader {
        val stream = javaClass.classLoader!!.getResourceAsStream(fileName)

        return BufferedReader(InputStreamReader(stream))
    }
}