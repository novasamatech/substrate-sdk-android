package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.Compact
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata.Companion.onlyInExtrinsic
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigInteger

private val NONCE_V0 = onlyInExtrinsic(DefaultSignedExtensions.CHECK_NONCE, Compact("Compact<Index>"))
private val NONCE_V1 = onlyInExtrinsic(DefaultSignedExtensions.CHECK_NONCE, u32)
private val TIP = onlyInExtrinsic(DefaultSignedExtensions.CHECK_TX_PAYMENT, Compact("Compact<u32>"))

@RunWith(MockitoJUnitRunner::class)
class ExtrasIncludedInExtrinsicTest {

    @Mock
    lateinit var runtime: RuntimeSnapshot

    @Mock
    lateinit var metadata: RuntimeMetadata

    @Mock
    lateinit var extrinsicMetadata: ExtrinsicMetadata

    @Before
    fun setup() {
        `when`(runtime.metadata).thenReturn(metadata)
        `when`(metadata.extrinsic).thenReturn(extrinsicMetadata)
    }

    @Test
    fun `should encode full set of extras`() {
        extrinsicContainsExtras(
            DefaultSignedExtensions.CHECK_MORTALITY,
            DefaultSignedExtensions.CHECK_NONCE,
            DefaultSignedExtensions.CHECK_TX_PAYMENT
        )

        val extras = mapOf(
            DefaultSignedExtensions.CHECK_TX_PAYMENT to BigInteger.ONE,
            DefaultSignedExtensions.CHECK_NONCE to BigInteger.TEN,
            DefaultSignedExtensions.CHECK_MORTALITY to Era.Immortal
        )

        val encoded = ExtrasIncludedInExtrinsic.toHex(runtime, extras)

        assertEquals("0x002804", encoded)
    }

    @Test
    fun `should encode partial set of extras and ignore unused arguments`() {
        extrinsicContainsExtras(
            DefaultSignedExtensions.CHECK_NONCE,
            DefaultSignedExtensions.CHECK_TX_PAYMENT
        )

        val extras = mapOf(
            DefaultSignedExtensions.CHECK_TX_PAYMENT to BigInteger.ONE,
            DefaultSignedExtensions.CHECK_NONCE to BigInteger.TEN,
            DefaultSignedExtensions.CHECK_MORTALITY to Era.Immortal // CheckMortality is unused
        )

        val encoded = ExtrasIncludedInExtrinsic.toHex(runtime, extras)

        assertEquals("0x2804", encoded)
    }

    @Test
    fun `should encode empty set of extras and ignore unused arguments`() {
        extrinsicContainsExtras()

        // all are unused
        val extras = mapOf(
            DefaultSignedExtensions.CHECK_TX_PAYMENT to BigInteger.ONE,
            DefaultSignedExtensions.CHECK_NONCE to BigInteger.TEN,
            DefaultSignedExtensions.CHECK_MORTALITY to Era.Immortal
        )

        val encoded = ExtrasIncludedInExtrinsic.toHex(runtime, extras)

        assertEquals("0x", encoded)
    }

    @Test
    fun `should require used extras`() {
        extrinsicContainsExtras(
            DefaultSignedExtensions.CHECK_NONCE,
            DefaultSignedExtensions.CHECK_TX_PAYMENT
        )

        val extras = mapOf(
            DefaultSignedExtensions.CHECK_TX_PAYMENT to BigInteger.ONE,
        )

        assertThrows<EncodeDecodeException> {
            ExtrasIncludedInExtrinsic.toHex(runtime, extras)
        }
    }

    @Test
    fun `should decode full set of extras`() {
        extrinsicContainsExtras(DefaultSignedExtensions.CHECK_MORTALITY, DefaultSignedExtensions.CHECK_NONCE, DefaultSignedExtensions.CHECK_TX_PAYMENT)

        val inHex = "0x002804"

        val decoded = ExtrasIncludedInExtrinsic.fromHex(runtime, inHex)

        assertEquals(decoded.size, 3)
    }

    @Test
    fun `should decode partial set of extras`() {
        extrinsicContainsExtras(DefaultSignedExtensions.CHECK_NONCE, DefaultSignedExtensions.CHECK_TX_PAYMENT)

        val inHex = "0x2804"

        val decoded = ExtrasIncludedInExtrinsic.fromHex(runtime, inHex)

        assertEquals(decoded.size, 2)
    }

    @Test
    fun `should decode empty set of extras`() {
        extrinsicContainsExtras()

        val inHex = "0x"

        val decoded = ExtrasIncludedInExtrinsic.fromHex(runtime, inHex)

        assertEquals(decoded.size, 0)
    }

    @Test
    fun `should encode extras according to requested extensions version`() {
        extrinsicContainsExtras(
            DefaultSignedExtensions.CHECK_MORTALITY,
            DefaultSignedExtensions.CHECK_NONCE,
            DefaultSignedExtensions.CHECK_TX_PAYMENT
        )
        extrinsicContainsExtras(
            DefaultSignedExtensions.CHECK_NONCE,
            DefaultSignedExtensions.CHECK_TX_PAYMENT,
            version = 1
        )

        val extras = mapOf(
            DefaultSignedExtensions.CHECK_TX_PAYMENT to BigInteger.ONE,
            DefaultSignedExtensions.CHECK_NONCE to BigInteger.TEN,
            DefaultSignedExtensions.CHECK_MORTALITY to Era.Immortal
        )

        assertEquals("0x002804", ExtrasIncludedInExtrinsic.toHex(runtime, extras, extensionsVersion = 0))
        assertEquals("0x2804", ExtrasIncludedInExtrinsic.toHex(runtime, extras, extensionsVersion = 1))
        // unversioned encode falls back to the default version
        assertEquals("0x002804", ExtrasIncludedInExtrinsic.toHex(runtime, extras))
    }

    @Test
    fun `should decode extras according to requested extensions version`() {
        extrinsicContainsExtras(
            DefaultSignedExtensions.CHECK_MORTALITY,
            DefaultSignedExtensions.CHECK_NONCE,
            DefaultSignedExtensions.CHECK_TX_PAYMENT
        )
        extrinsicContainsExtras(
            DefaultSignedExtensions.CHECK_NONCE,
            DefaultSignedExtensions.CHECK_TX_PAYMENT,
            version = 1
        )

        val decodedV0 = ExtrasIncludedInExtrinsic.fromHex(runtime, "0x002804", extensionsVersion = 0)
        assertEquals(
            setOf(DefaultSignedExtensions.CHECK_MORTALITY, DefaultSignedExtensions.CHECK_NONCE, DefaultSignedExtensions.CHECK_TX_PAYMENT),
            decodedV0.keys
        )

        val decodedV1 = ExtrasIncludedInExtrinsic.fromHex(runtime, "0x2804", extensionsVersion = 1)
        assertEquals(setOf(DefaultSignedExtensions.CHECK_NONCE, DefaultSignedExtensions.CHECK_TX_PAYMENT), decodedV1.keys)
        assertEquals(BigInteger.TEN, decodedV1[DefaultSignedExtensions.CHECK_NONCE])
    }

    @Test
    fun `should encode extension with the same id by the shape of the requested version`() {
        extrinsicContainsExtrasMetadata(NONCE_V0, TIP, version = 0)
        // v1 changed CheckNonce from compact to fixed-width u32
        extrinsicContainsExtrasMetadata(NONCE_V1, TIP, version = 1)

        val extras = mapOf(
            DefaultSignedExtensions.CHECK_NONCE to BigInteger.TEN,
            DefaultSignedExtensions.CHECK_TX_PAYMENT to BigInteger.ONE,
        )

        // compact(10) = 0x28, compact(1) = 0x04
        assertEquals("0x2804", ExtrasIncludedInExtrinsic.toHex(runtime, extras, extensionsVersion = 0))
        // u32(10) LE = 0x0a000000, compact(1) = 0x04
        assertEquals("0x0a00000004", ExtrasIncludedInExtrinsic.toHex(runtime, extras, extensionsVersion = 1))
    }

    @Test
    fun `should decode extension with the same id by the shape of the requested version`() {
        extrinsicContainsExtrasMetadata(NONCE_V0, TIP, version = 0)
        extrinsicContainsExtrasMetadata(NONCE_V1, TIP, version = 1)

        val decodedV0 = ExtrasIncludedInExtrinsic.fromHex(runtime, "0x2804", extensionsVersion = 0)
        assertEquals(setOf(DefaultSignedExtensions.CHECK_NONCE, DefaultSignedExtensions.CHECK_TX_PAYMENT), decodedV0.keys)
        assertEquals(BigInteger.TEN, decodedV0[DefaultSignedExtensions.CHECK_NONCE])
        assertEquals(BigInteger.ONE, decodedV0[DefaultSignedExtensions.CHECK_TX_PAYMENT])

        val decodedV1 = ExtrasIncludedInExtrinsic.fromHex(runtime, "0x0a00000004", extensionsVersion = 1)
        assertEquals(setOf(DefaultSignedExtensions.CHECK_NONCE, DefaultSignedExtensions.CHECK_TX_PAYMENT), decodedV1.keys)
        assertEquals(BigInteger.TEN, decodedV1[DefaultSignedExtensions.CHECK_NONCE])
        assertEquals(BigInteger.ONE, decodedV1[DefaultSignedExtensions.CHECK_TX_PAYMENT])
    }

    @Test
    fun `should not decode bytes of one version using shape of another`() {
        extrinsicContainsExtrasMetadata(NONCE_V0, TIP, version = 0)
        extrinsicContainsExtrasMetadata(NONCE_V1, TIP, version = 1)

        // v1 bytes decoded as v0: compact treats 0x0a as 4-byte mode and consumes 0x0a000000 -> 2, then 0x04 -> 1
        val misdecoded = ExtrasIncludedInExtrinsic.fromHex(runtime, "0x0a00000004", extensionsVersion = 0)
        assertEquals(BigInteger.valueOf(2), misdecoded[DefaultSignedExtensions.CHECK_NONCE])
        assertEquals(BigInteger.ONE, misdecoded[DefaultSignedExtensions.CHECK_TX_PAYMENT])
    }

    @Test
    fun `should fail for unsupported extensions version`() {
        `when`(extrinsicMetadata.transactionExtensionsOrThrow(5)).thenThrow(IllegalArgumentException())

        assertThrows<IllegalArgumentException> {
            ExtrasIncludedInExtrinsic.toHex(runtime, emptyMap(), extensionsVersion = 5)
        }
    }

    private fun ExtrinsicPayloadExtras.toHex(
        runtime: RuntimeSnapshot,
        value: ExtrinsicPayloadExtrasInstance,
        extensionsVersion: Byte
    ): String {
        val writer = java.io.ByteArrayOutputStream()
        encode(ScaleCodecWriter(writer), runtime, value, extensionsVersion)
        return writer.toByteArray().toHexString(withPrefix = true)
    }

    private fun ExtrinsicPayloadExtras.fromHex(
        runtime: RuntimeSnapshot,
        hex: String,
        extensionsVersion: Byte
    ): ExtrinsicPayloadExtrasInstance {
        return decode(ScaleCodecReader(hex.fromHex()), runtime, extensionsVersion)
    }

    private fun extrinsicContainsExtrasMetadata(
        vararg extras: TransactionExtensionMetadata,
        version: Int
    ) {
        `when`(extrinsicMetadata.transactionExtensionsOrThrow(version)).thenReturn(extras.toList())
    }

    private fun extrinsicContainsExtras(
        vararg extras: String,
        version: Int = ExtrinsicMetadata.DEFAULT_TRANSACTION_EXTENSION_VERSION
    ) {
        val signedExtensions = DefaultSignedExtensions.ALL.filter { it.id in extras }
        `when`(extrinsicMetadata.transactionExtensionsOrThrow(version)).thenReturn(signedExtensions)
    }
}