package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.encrypt.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.integration.transfer
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.multiAddressFromId
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

private val KEYPAIR = BaseKeypair(
    publicKey = "fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168".fromHex(),
    privateKey = "f3923eea431177cd21906d4308aea61c037055fb00575cae687217c6d8b2397f".fromHex()
)

class ExtrinsicBuilderTest {

    val runtime = RealRuntimeProvider.buildRuntime("westend")

    @Test
    fun `should build single sora transfer extrinsic`() {
        val soraRuntime = RealRuntimeProvider.buildRuntime("sora2")
        val soraKeypair = BaseKeypair(
            privateKey = "dd9b35e3288c2e2667313532f825f60fc5e8523b16d8e3ddc0b0ff5200b4c145".fromHex(),
            publicKey = "83ba494b62a40d20c370e5381230d74b4e8906d0334a91777baef57c9a935467".fromHex()
        )
        val from = "5F3RU8neUpkZJK7QxAHJ9TGDjUiyjfufpZvaXDBEifPkeJSz"
        val to = "5EcDoG4T1SLbop4bxBjLL9VJaaytZxGXA7mLaY9y84GYpzsR"
        val asset = "0200000000000000000000000000000000000000000000000000000000000000"
        val extrinsicInHex =
            "0xe1028483ba494b62a40d20c370e5381230d74b4e8906d0334a91777baef57c9a935467007a3855dd10d316c70dad4e4b88a857e2994017fac758f153d8f2cda5aba8cfbbbdd1cc3e73aa8b639bc6d45e151b61baa1f797370928b00d6fb7069ee6b1620f25000400140102000000000000000000000000000000000000000000000000000000000000007081dd99c361e7ccd05171ae67f7adcf2da5ea102ee65670db0d1190c7429674000014bbf08ac6020000000000000000"

        val builder = ExtrinsicBuilder(
            runtime = soraRuntime,
            keypair = soraKeypair,
            nonce = 1.toBigInteger(),
            runtimeVersion = RuntimeVersion(1, 1),
            genesisHash = "0f751ca2d30efe3385a4001d0bfa1548471babf5095f6fe88ee4813cf724fafc".fromHex(),
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.ED25519),
            accountIdentifier = from.toAccountId(),
            era = Era.getEraFromBlockPeriod(44866, 64),
            blockHash = "0xa532ea14451c9b4e1a9ed1c75ab67d8be659362c9d8f2206009ae8d62faf9fca".fromHex()
        )

        builder.call(
            "Assets",
            "transfer",
            mapOf(
                "asset_id" to asset.fromHex(),
                "to" to to.toAccountId(),
                "amount" to BigInteger("200000000000000000")
            )
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }

    @Test
    fun `should build single transfer extrinsic`() {
        val extrinsicInHex =
            "0x41028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d16800080bfe8bc67f44b498239887dc5679523cfcb1d20fd9ec9d6bae0a385cca118d2cb7ef9f4674d52a810feb32932d7c6fe3e05ce9e06cd72cf499c8692206410ab5038800040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

        val builder = createExtrinsicBuilder()

        builder.transfer(
            recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
            amount = BigInteger("10000000000")
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }

    @Test
    fun `should build batch extrinsic`() {

        val extrinsicInHex =
            "0xf1028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168005b94d4436372ba74895936695e97d543358219e77f3e827f77b2e26f53413363a5dd098e172a51308e7d35aa6c03c5f171c4b43732db61c3d86b62d83e626b07b5038800100008040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

        val builder = createExtrinsicBuilder()

        repeat(2) {
            builder.transfer(
                recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                amount = BigInteger("10000000000")
            )
        }

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }

    @Test
    fun `should build batch_all extrinsic`() {
        val extrinsicBuilder = createExtrinsicBuilder()

        repeat(2) {
            extrinsicBuilder.transfer(
                recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                amount = BigInteger("10000000000")
            )
        }

        val encoded = extrinsicBuilder.build(useBatchAll = true)
        val decoded = Extrinsic.fromHex(runtime, encoded)

        assertEquals(decoded.call.function.name, "batch_all")
    }

    private fun createExtrinsicBuilder()  = ExtrinsicBuilder(
        runtime = runtime,
        keypair = KEYPAIR,
        nonce = 34.toBigInteger(),
        runtimeVersion = RuntimeVersion(48, 4),
        genesisHash = "e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e".fromHex(),
        multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.ED25519),
        accountIdentifier = multiAddressFromId(KEYPAIR.publicKey),
        era = Era.Mortal(64, 59),
        blockHash = "0x1b876104c68b4a8924c098d61d2ad798761bb6fff55cca2885939ffc27ef5ecb".fromHex()
    )
}