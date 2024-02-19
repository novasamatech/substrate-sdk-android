package io.novasama.substrate_sdk_android.integration.extrinsic

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.keypair.BaseKeypair
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.integration.BaseIntegrationTest
import io.novasama.substrate_sdk_android.integration.WESTEND_URL
import io.novasama.substrate_sdk_android.integration.transfer
import io.novasama.substrate_sdk_android.runtime.RealRuntimeProvider
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicBuilder
import io.novasama.substrate_sdk_android.runtime.extrinsic.Nonce
import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.KeyPairSigner
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.publicKeyToSubstrateAccountId
import io.novasama.substrate_sdk_android.wsrpc.executeAsync
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.author.SubmitExtrinsicRequest
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain.RuntimeVersion
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import java.math.BigInteger

private val KEYPAIR = BaseKeypair(
    publicKey = "fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168".fromHex(),
    privateKey = "f3923eea431177cd21906d4308aea61c037055fb00575cae687217c6d8b2397f".fromHex()
)

@Ignore("Manual run only")
class SendIntegrationTest : BaseIntegrationTest(WESTEND_URL) {

    val runtime = RealRuntimeProvider.buildRuntime("westend")

    @Test
    fun `should form batch extrinsic so node accepts it`() = runBlocking {
        val builder = ExtrinsicBuilder(
            runtime = runtime,
            signer = KeyPairSigner(
                keypair = KEYPAIR,
                encryption = MultiChainEncryption.Substrate(EncryptionType.ED25519)
            ),
            nonce = Nonce.singleTx(38.toBigInteger()),
            runtimeVersion = RuntimeVersion(48, 4),
            genesisHash = "e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e".fromHex(),
            accountId = KEYPAIR.publicKey.publicKeyToSubstrateAccountId(),
        )

        repeat(2) {
            builder.transfer(
                recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                amount = BigInteger("5000000001")
            )
        }

        val extrinsic = builder.build()

        print(socketService.executeAsync(SubmitExtrinsicRequest(extrinsic)).result!!)
    }
}

