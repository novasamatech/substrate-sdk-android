package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.common.assertThrows
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.xxHash128
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

private val MODULE_NAME = "Test"
private val CALL_NAME = "Test"

private val PREFIX =
    (MODULE_NAME.encodeToByteArray().xxHash128() + CALL_NAME.encodeToByteArray().xxHash128())
        .toHexString(withPrefix = true)

@RunWith(MockitoJUnitRunner::class)
class RuntimeMetadataExtKtTest {

    @Mock
    lateinit var runtime: RuntimeSnapshot

    @Test
    fun `test plain storage`() {
        val storageEntry = storageEntry(StorageEntryType.Plain(value = BooleanType))

        assertEquals(PREFIX, storageEntry.storageKey())

        assertThrows<IllegalArgumentException> {
            storageEntry.storageKey(runtime, false)
        }
    }

    @Test
    fun `test nmap`() {
        val storageEntry = storageEntry(
            StorageEntryType.NMap(
                value = BooleanType,
                keys = listOf(BooleanType, BooleanType, BooleanType),
                hashers = listOf(
                    StorageHasher.Identity,
                    StorageHasher.Identity,
                    StorageHasher.Identity
                )
            )
        )

        assertEquals(PREFIX, storageEntry.storageKey(runtime))
        assertEquals(PREFIX + "01", storageEntry.storageKey(runtime, true))
        assertEquals(PREFIX + "0100", storageEntry.storageKey(runtime, true, false))
        assertEquals(PREFIX + "010001", storageEntry.storageKey(runtime, true, false, true))

        assertThrows<IllegalArgumentException> {
            storageEntry.storageKey(runtime, false, false, false, false)
        }
    }

    private fun storageEntry(storageEntryType: StorageEntryType): StorageEntry {
        val mock = Mockito.mock(StorageEntry::class.java)

        Mockito.`when`(mock.type).thenReturn(storageEntryType)
        Mockito.`when`(mock.moduleName).thenReturn(MODULE_NAME)
        Mockito.`when`(mock.name).thenReturn(CALL_NAME)

        return mock
    }
}