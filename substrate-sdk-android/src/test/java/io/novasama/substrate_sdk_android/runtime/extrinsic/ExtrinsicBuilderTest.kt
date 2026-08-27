package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.keypair.BaseKeypair
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.integration.transfer
import io.novasama.substrate_sdk_android.runtime.RealRuntimeProvider
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Era
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.extrinsic.builder.ExtrinsicBuilder
import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.KeyPairSigner
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.ChargeAssetTxPayment
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignatureMode
import io.novasama.substrate_sdk_android.runtime.metadata.MetadataTestCommon
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.publicKeyToSubstrateAccountId
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAccountId
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain.RuntimeVersion
import kotlinx.coroutines.test.runBlockingTest
import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

private val KEYPAIR = BaseKeypair(
    publicKey = "fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168".fromHex(),
    privateKey = "f3923eea431177cd21906d4308aea61c037055fb00575cae687217c6d8b2397f".fromHex()
)

private const val SINGLE_TRANSFER_EXTRINSIC =
    "0x41028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d16800080bfe8bc67f44b498239887dc5679523cfcb1d20fd9ec9d6bae0a385cca118d2cb7ef9f4674d52a810feb32932d7c6fe3e05ce9e06cd72cf499c8692206410ab5038800040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

private const val EXTRINSIC_SIGNATURE =
    "0x00080bfe8bc67f44b498239887dc5679523cfcb1d20fd9ec9d6bae0a385cca118d2cb7ef9f4674d52a810feb32932d7c6fe3e05ce9e06cd72cf499c8692206410a"

private const val BIG_TRANSACTION =
    "0x790e8400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168008d6ba41aa56d09071bbd96c1aa0378000b8a048699d5b751a27a01e7c3ce955363005df217f3ef1edb63d4c26787e8743d3797a1cf76fcaf3ae4899bd2cc660eb5038800100250040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

private const val V5_TRANSFER_EXTRINSIC =
    "0xc04500b503880000040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

private fun ExtrinsicBuilder.testSingleTransfer(): ExtrinsicBuilder {
    return transfer(
        recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
        amount = BigInteger("10000000000")
    )
}

class ExtrinsicBuilderTest {

    val runtime = RealRuntimeProvider.buildRuntime("westend")

    @Test
    fun `should build single transfer extrinsic`() = runBlockingTest {
        val extrinsic = createExtrinsicBuilder()
            .testSingleTransfer()
            .buildExtrinsic()

        assertEquals(SINGLE_TRANSFER_EXTRINSIC, extrinsic.extrinsicHex)
    }

    @Test
    fun `should build extrinsic signature from call instance`() = runBlockingTest {
        val extrinsic = createExtrinsicBuilder()
            .testSingleTransfer()
            .buildExtrinsic()

        assertEquals(EXTRINSIC_SIGNATURE, extrinsic.signatureHex)
    }

    @Test
    fun `should replace call`() = runBlockingTest {
        val wrongAMount = "123".toBigInteger()
        val recipientAccountId =
            "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex()

        val extrinsic = createExtrinsicBuilder()
            .transfer(
                recipientAccountId = recipientAccountId,
                amount = wrongAMount
            )
            .resetCalls()
            .testSingleTransfer()
            .buildExtrinsic()

        assertEquals(SINGLE_TRANSFER_EXTRINSIC, extrinsic.extrinsicHex)
    }

    @Test
    fun `should build batch extrinsic`() = runBlockingTest {
        val extrinsicInHex =
            "0xf1028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d168005b94d4436372ba74895936695e97d543358219e77f3e827f77b2e26f53413363a5dd098e172a51308e7d35aa6c03c5f171c4b43732db61c3d86b62d83e626b07b5038800100008040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402040000340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c630700e40b5402"

        val builder = createExtrinsicBuilder()

        repeat(2) {
            builder.transfer(
                recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                amount = BigInteger("10000000000")
            )
        }

        val extrinsic = builder.buildExtrinsic()

        assertEquals(extrinsicInHex, extrinsic.extrinsicHex)
    }

    @Test
    fun `should build batch_all extrinsic`() = runBlockingTest {
        val extrinsicBuilder = createExtrinsicBuilder(batchMode = BatchMode.BATCH_ALL)

        repeat(2) {
            extrinsicBuilder.transfer(
                recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                amount = BigInteger("10000000000")
            )
        }

        val extrinsic = extrinsicBuilder.buildExtrinsic()
        val decoded = Extrinsic.fromHex(runtime, extrinsic.extrinsicHex)

        assertEquals(decoded.call.function.name, "batch_all")
    }

    @Test
    fun `should build big extrinsic`() = runBlockingTest {
        val extrinsicBuilder = createExtrinsicBuilder(batchMode = BatchMode.BATCH_ALL)

        repeat(20) {
            extrinsicBuilder.transfer(
                recipientAccountId = "340a806419d5e278172e45cb0e50da1b031795366c99ddfe0a680bd53b142c63".fromHex(),
                amount = BigInteger("10000000000")
            )
        }

        val extrinsic = extrinsicBuilder.buildExtrinsic()

        assertEquals(BIG_TRANSACTION, extrinsic.extrinsicHex)
    }

    @Test
    fun `should register custom signed extensions`() = runBlockingTest {
        val runtime = RealRuntimeProvider.buildRuntimePostV14("statemine")

        val extrinsicInHex =
            "0x45028400fdc41550fb5186d71cae699c31731b3e1baa10680c7bd6b3831a6d222cf4d1680045ba1f9d291fff7dddf36f7ec060405d5e87ac8fab8832cfcc66858e6975141748ce89c41bda6c3a84204d3c6f929b928702168ca38bbed69b172044b599a10ab5038800000a0000bcc5ecf679ebd776866a04c212a4ec5dc45cefab57d7aa858c389844e212693f0700e40b5402"

        val builder = ExtrinsicBuilder(
            runtime = runtime,
            nonce = 34.toBigInteger(),
            runtimeVersion = RuntimeVersion(601, 4),
            genesisHash = "48239ef607d7928874027a43a67689209727dfb3d3dc5e5b03a39bdc2eda771a".fromHex(),
            signer = keypairSigner(),
            accountId = KEYPAIR.publicKey.publicKeyToSubstrateAccountId(),
            era = Era.Mortal(64, 59),
            blockHash = "0xdd7532c5c01242696001e57cded1bc1326379059300287552a9c344e5bea1070".fromHex()
        )

        builder.transfer(
            recipientAccountId = "GqqKJJZ1MtiWiC6CzNg3g8bawriq6HZioHW1NEpxdf6Q6P5".toAccountId(),
            amount = BigInteger("10000000000")
        )

        builder.setTransactionExtension(
            ChargeAssetTxPayment(tip = BigInteger.ZERO, assetId = null)
        )

        val extrinsic = builder.buildExtrinsic()

        assertEquals(extrinsicInHex, extrinsic.extrinsicHex)
    }

    @Test
    fun `should build extrinsic with check metadata extension`() = runBlockingTest {
        val runtime = MetadataTestCommon.buildPost14TestRuntime("check_metadata_runtime_v15")

        createExtrinsicBuilder(runtime)
            .testSingleTransfer()
            .buildExtrinsic()
    }

    @Test
    fun `should not allow to add call to itself`() = runBlockingTest {
        val extrinsicBuilder = createExtrinsicBuilder()
        repeat(2) {
            extrinsicBuilder.testSingleTransfer()
        }

        val call = extrinsicBuilder.getWrappedCall()
        extrinsicBuilder.call(call)

        // This will fail with StackOverflow is call was added to itself
        extrinsicBuilder.buildExtrinsic()
    }

    @Test
    fun `should build v5 general transaction`() = runBlockingTest {
        val runtime = RealRuntimeProvider.buildRuntimePostV14("westend_v15")

        val expectedTx = V5_TRANSFER_EXTRINSIC

        val extrinsicBuilder = createExtrinsicBuilder(
            usedRuntime = runtime,
            extrinsicVersion = ExtrinsicVersion.V5()
        )

        val extrinsic = extrinsicBuilder.testSingleTransfer()
            .buildExtrinsic()
            .extrinsicHex

        assertEquals(expectedTx, extrinsic)
    }

    @Test
    fun `should build v5 general transaction using requested extensions version`() = runBlockingTest {
        val runtime = RealRuntimeProvider.buildRuntimePostV14("westend_v15").withExtensionVersionWithoutTip()

        val extrinsicV0 = createExtrinsicBuilder(usedRuntime = runtime, extrinsicVersion = ExtrinsicVersion.V5())
            .testSingleTransfer()
            .buildExtrinsic()
            .extrinsicHex

        val extrinsicV1 = createExtrinsicBuilder(usedRuntime = runtime, extrinsicVersion = ExtrinsicVersion.V5(extensionVersion = 1))
            .testSingleTransfer()
            .buildExtrinsic()
            .extrinsicHex

        // Default V5() must still produce version 0 encoding, regardless of the latest version present in metadata
        assertEquals(V5_TRANSFER_EXTRINSIC, extrinsicV0)

        val decodedV0 = Extrinsic.fromHex(runtime, extrinsicV0).type as Extrinsic.ExtrinsicType.GeneralTransaction
        val decodedV1 = Extrinsic.fromHex(runtime, extrinsicV1).type as Extrinsic.ExtrinsicType.GeneralTransaction

        assertEquals(0.toByte(), decodedV0.extensionsVersion)
        assertEquals(1.toByte(), decodedV1.extensionsVersion)

        assertTrue(DefaultSignedExtensions.CHECK_TX_PAYMENT in decodedV0.extensionExplicits)
        assertFalse(DefaultSignedExtensions.CHECK_TX_PAYMENT in decodedV1.extensionExplicits)
        assertEquals(
            decodedV0.extensionExplicits.keys - DefaultSignedExtensions.CHECK_TX_PAYMENT,
            decodedV1.extensionExplicits.keys
        )

        // Bytes differ exactly by extensions version byte and the dropped single-byte tip (compact 0)
        assertEquals(extrinsicV0.fromHex().size - 1, extrinsicV1.fromHex().size)
    }

    @Test
    fun `should fail to build v5 general transaction for unsupported extensions version`() = runBlockingTest {
        val runtime = RealRuntimeProvider.buildRuntimePostV14("westend_v15")

        assertThrows<IllegalArgumentException> {
            runBlockingTest {
                createExtrinsicBuilder(usedRuntime = runtime, extrinsicVersion = ExtrinsicVersion.V5(extensionVersion = 7))
                    .testSingleTransfer()
                    .buildExtrinsic()
            }
        }
    }

    /**
     * Adds transaction extensions version 1 that contains all extensions except ChargeTransactionPayment,
     * so version 1 becomes the latest
     */
    private fun RuntimeSnapshot.withExtensionVersionWithoutTip(): RuntimeSnapshot {
        val extrinsic = metadata.extrinsic
        val allExtensions = extrinsic.transactionExtensions
        val withoutTip = allExtensions.indices.filter { allExtensions[it].id != DefaultSignedExtensions.CHECK_TX_PAYMENT }

        val patchedExtrinsic = ExtrinsicMetadata(
            versions = extrinsic.versions,
            transactionExtensions = allExtensions,
            transactionExtensionsByVersion = extrinsic.transactionExtensionsByVersion + (1 to withoutTip)
        )
        val patchedMetadata = RuntimeMetadata(
            metadataVersion = metadata.metadataVersion,
            modules = metadata.modules,
            extrinsic = patchedExtrinsic,
            apis = metadata.apis
        )

        return RuntimeSnapshot(typeRegistry, patchedMetadata)
    }

    private fun createExtrinsicBuilder(
        usedRuntime: RuntimeSnapshot = runtime,
        batchMode: BatchMode = BatchMode.BATCH,
        extrinsicVersion: ExtrinsicVersion = ExtrinsicVersion.V4
    ) = ExtrinsicBuilder(
        runtime = usedRuntime,
        signer = keypairSigner(),
        nonce = 34.toBigInteger(),
        runtimeVersion = RuntimeVersion(48, 4),
        genesisHash = "e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e".fromHex(),
        accountId = KEYPAIR.publicKey.publicKeyToSubstrateAccountId(),
        era = Era.Mortal(64, 59),
        blockHash = "0x1b876104c68b4a8924c098d61d2ad798761bb6fff55cca2885939ffc27ef5ecb".fromHex(),
        batchMode = batchMode,
        extrinsicVersion = extrinsicVersion
    )

    private fun keypairSigner() = KeyPairSigner(
        keypair = KEYPAIR,
        encryption = MultiChainEncryption.Substrate(EncryptionType.ED25519)
    )
}