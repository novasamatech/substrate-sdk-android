package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.any
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.TestSignatureConstructor
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic
import jp.co.soramitsu.fearless_utils.signing.MultiSignature
import jp.co.soramitsu.fearless_utils.signing.Signer
import jp.co.soramitsu.fearless_utils.signing.SignerPayloadRaw
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.publicKeyToAccountId
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigInteger

private val ORIGIN_ACCOUNT_ID =
    "fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168".fromHex().publicKeyToAccountId()

private const val SINGLE_TRANSFER_EXTRINSIC =
    "0x41028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d16800080bfe8bc67f44b498239887dc5679523cfcb1d20fd9ec9d6bae0a385cca118d2cb7ef9f4674d52a810feb32932d7c6fe3e05ce9e06cd72cf499c8692206410ab5038800040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"
private val TRANSFER_CALL_BYTES =
    "0x040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402".fromHex()
private const val EXTRINSIC_SIGNATURE =
    "0x00080bfe8bc67f44b498239887dc5679523cfcb1d20fd9ec9d6bae0a385cca118d2cb7ef9f4674d52a810feb32932d7c6fe3e05ce9e06cd72cf499c8692206410a"

private fun ExtrinsicBuilder.transfer(
    recipientAccountId: ByteArray,
    amount: BigInteger
): ExtrinsicBuilder {
    return call(
        moduleName = "Balances",
        callName = "transfer",
        arguments = mapOf(
            "dest" to DictEnum.Entry(
                name = "Id",
                value = recipientAccountId
            ),
            "value" to amount
        )
    )
}

private fun ExtrinsicBuilder.testSingleTransfer(): ExtrinsicBuilder {
    return transfer(
        recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
        amount = BigInteger("10000000000")
    )
}

@RunWith(MockitoJUnitRunner::class)
class ExtrinsicBuilderTest {

    val runtime = RealRuntimeProvider.buildRuntime("westend")

    @Mock
    lateinit var signer: Signer

    @Test
    fun `should build extrinsic from raw call bytes`() {
        signerSignsTransfer()

        val extrinsic = createExtrinsicBuilder()
            .build(TRANSFER_CALL_BYTES)

        assertEquals(SINGLE_TRANSFER_EXTRINSIC, extrinsic)
    }

    @Test
    fun `should build single transfer extrinsic`() {
        signerSignsTransfer()

        val encoded = createExtrinsicBuilder()
            .testSingleTransfer()
            .build()

        assertEquals(SINGLE_TRANSFER_EXTRINSIC, encoded)
    }

    @Test
    fun `should build extrinsic signature from call instance`() {
        signerSignsTransfer()

        val actualSignature = createExtrinsicBuilder()
            .testSingleTransfer()
            .buildSignature()

        assertEquals(EXTRINSIC_SIGNATURE, actualSignature)
    }

    @Test
    fun `should build extrinsic signature from raw call bytes`() {
        signerSignsTransfer()

        val extrinsic = createExtrinsicBuilder()
            .buildSignature(TRANSFER_CALL_BYTES)

        assertEquals(EXTRINSIC_SIGNATURE, extrinsic)
    }

    @Test
    fun `should replace call`() {
        signerSignsTransfer()

        val wrongAMount = "123".toBigInteger()
        val recipientAccountId =
            "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex()

        val encoded = createExtrinsicBuilder()
            .transfer(
                recipientAccountId = recipientAccountId,
                amount = wrongAMount
            )
            .reset()
            .testSingleTransfer()
            .build()

        assertEquals(SINGLE_TRANSFER_EXTRINSIC, encoded)
    }

    @Test
    fun `should build batch extrinsic`() {
        signerSigns(
            message = "100008040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402b50388003000000004000000e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e1b876104c68b4a8924c098d61d2ad798761bb6fff55cca2885939ffc27ef5ecb",
            signature = "5b94d4436372ba74895936695e97d543358219e77f3e827f77b2e26f53413363a5dd098e172a51308e7d35aa6c03c5f171c4b43732db61c3d86b62d83e626b07"
        )

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
        signerSigns(
            message = "100208040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402b50388003000000004000000e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e1b876104c68b4a8924c098d61d2ad798761bb6fff55cca2885939ffc27ef5ecb",
            signature = "c7353b1002c1b62e881f3013f5a01e22178e276cb9a5157ee4b0f21baf9199deaeb905f5dc42d2554dc8d1bbfc2cee1e1df293122b392b8ed83647483d104b00"
        )

        val extrinsicBuilder = createExtrinsicBuilder()

        repeat(2) {
            extrinsicBuilder.transfer(
                recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                amount = BigInteger("10000000000")
            )
        }

        val encoded = extrinsicBuilder.build(useBatchAll = true)
        val decoded = Extrinsic().fromHex(runtime, encoded)

        assertEquals(decoded.call.function.name, "batch_all")
    }

    @Test
    fun `should build single transfer extrinsic statemine`() {
        signerSigns(
            message = "0a0000bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f0700e40b5402b503880000590200000400000048239ef607d7928874027a43a67689209727dfb3d3dc5e5b03a39bdc2eda771add7532c5c01242696001e57cded1bc1326379059300287552a9c344e5bea1070",
            signature = "45ba1f9d291fff7dddf36f7ec060405d5e87ac8fab8832cfcc66858e6975141748ce89c41bda6c3a84204d3c6f929b928702168ca38bbed69b172044b599a10a"
        )

        val runtime = RealRuntimeProvider.buildRuntimeV14("statemine")

        val extrinsicInHex =
            "0x45028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d1680045ba1f9d291fff7dddf36f7ec060405d5e87ac8fab8832cfcc66858e6975141748ce89c41bda6c3a84204d3c6f929b928702168ca38bbed69b172044b599a10ab5038800000a0000bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f0700e40b5402"

        val chargeAssetTxPayment = SignedExtension(
            name = "ChargeAssetTxPayment",
            type = runtime.typeRegistry["pallet_asset_tx_payment.ChargeAssetTxPayment"]!!
        )

        val builder = ExtrinsicBuilder(
            runtime = runtime,
            nonce = 34.toBigInteger(),
            runtimeVersion = RuntimeVersion(601, 4),
            genesisHash = "48239ef607d7928874027a43a67689209727dfb3d3dc5e5b03a39bdc2eda771a".fromHex(),
            era = Era.Mortal(64, 59),
            customSignedExtensions = mapOf(
                chargeAssetTxPayment to Struct.Instance(
                    mapOf(
                        "tip" to BigInteger.ZERO,
                        "assetId" to null
                    )
                )
            ),
            blockHash = "0xdd7532c5c01242696001e57cded1bc1326379059300287552a9c344e5bea1070".fromHex(),
            signer = signer,
            signatureConstructor = TestSignatureConstructor,
            origin = ORIGIN_ACCOUNT_ID
        )

        builder.transfer(
            recipientAccountId = "GqqKJJZ1MtiWiC6CzNg3g8bawriq6HZioHW1NEpxdf6Q6P5".toAccountId(),
            amount = BigInteger("10000000000")
        )

        val encoded = builder.build()

        assertEquals(extrinsicInHex, encoded)
    }

    private fun signerSignsTransfer() = signerSigns(
        message = "040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402b50388003000000004000000e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e1b876104c68b4a8924c098d61d2ad798761bb6fff55cca2885939ffc27ef5ecb",
        signature = "080bfe8bc67f44b498239887dc5679523cfcb1d20fd9ec9d6bae0a385cca118d2cb7ef9f4674d52a810feb32932d7c6fe3e05ce9e06cd72cf499c8692206410a"
    )

    private fun signerSigns(
        message: String,
        signature: String
    ) {
        `when`(signer.signRaw(any())).thenAnswer {
            val incomingMessage = it.arguments.first() as SignerPayloadRaw

            assertArrayEquals(message.fromHex(), incomingMessage.data)

            MultiSignature(
                encryptionType = "Ed25519",
                signature = signature.fromHex()
            )
        }
    }

    private fun createExtrinsicBuilder() = ExtrinsicBuilder(
        runtime = runtime,
        nonce = 34.toBigInteger(),
        runtimeVersion = RuntimeVersion(48, 4),
        genesisHash = "e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e".fromHex(),
        era = Era.Mortal(64, 59),
        blockHash = "0x1b876104c68b4a8924c098d61d2ad798761bb6fff55cca2885939ffc27ef5ecb".fromHex(),
        signer = signer,
        signatureConstructor = TestSignatureConstructor,
        origin = ORIGIN_ACCOUNT_ID
    )
}
