package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class ExtrinsicMetadataTest {

    private val allExtensions = DefaultSignedExtensions.ALL

    // version 0 -> all extensions, version 1 -> all except ChargeTransactionPayment
    private val metadata = ExtrinsicMetadata(
        versions = listOf(BigInteger.valueOf(4), BigInteger.valueOf(5)),
        transactionExtensions = allExtensions,
        transactionExtensionsByVersion = mapOf(
            0 to allExtensions.indices.toList(),
            1 to allExtensions.indices.filter { allExtensions[it].id != DefaultSignedExtensions.CHECK_TX_PAYMENT }
        )
    )

    @Test
    fun `should resolve extensions by version`() {
        assertEquals(allExtensions.map { it.id }, metadata.transactionExtensions(0).map { it.id })

        val v1Ids = metadata.transactionExtensions(1).map { it.id }
        assertEquals(allExtensions.map { it.id } - DefaultSignedExtensions.CHECK_TX_PAYMENT, v1Ids)
    }

    @Test
    fun `should return empty list for unsupported version`() {
        assertTrue(metadata.transactionExtensions(2).isEmpty())
    }

    @Test
    fun `should throw for unsupported version in strict accessor`() {
        assertThrows<IllegalArgumentException> {
            metadata.transactionExtensionsOrThrow(2)
        }
    }

    @Test
    fun `latest extensions should use the highest version`() {
        assertEquals(metadata.transactionExtensions(1).map { it.id }, metadata.latestTransactionExtensions.map { it.id })
    }

    @Test
    fun `legacy constructor should register extensions under default version`() {
        val legacy = ExtrinsicMetadata(version = BigInteger.valueOf(4), signedExtensions = allExtensions)

        assertEquals(mapOf(ExtrinsicMetadata.DEFAULT_TRANSACTION_EXTENSION_VERSION to allExtensions.indices.toList()), legacy.transactionExtensionsByVersion)
        assertEquals(allExtensions.map { it.id }, legacy.latestTransactionExtensions.map { it.id })
    }
}
