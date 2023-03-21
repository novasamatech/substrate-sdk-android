package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.hash.Hasher.xxHash128
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Null
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.DynamicByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntry
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntryType
import io.novasama.substrate_sdk_android.runtime.metadata.module.MetadataFunction
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import org.junit.Assert.assertArrayEquals
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

    @Test
    fun `test storageKeys()`() {
        val storageEntry = storageEntry(
            StorageEntryType.NMap(
                value = BooleanType,
                keys = listOf(BooleanType, BooleanType),
                hashers = listOf(
                    StorageHasher.Identity,
                    StorageHasher.Identity,
                )
            )
        )

        val arguments = listOf(
            listOf(false, false),
            listOf(false, true),
            listOf(true, false),
            listOf(true, true),
        )

        val expectedKeys = listOf(
            PREFIX + "0000",
            PREFIX + "0001",
            PREFIX + "0100",
            PREFIX + "0101",
        )

        val storageKeys = storageEntry.storageKeys(runtime, arguments)

        expectedKeys.zip(storageKeys).forEach { (expected, actual) ->
            assertEquals(expected, actual)
        }

        // should check dimensionality
        assertThrows<IllegalArgumentException> {
            storageEntry.storageKeys(runtime, listOf(emptyList()))
            storageEntry.storageKeys(runtime, listOf(listOf(true)))
            storageEntry.storageKeys(runtime, listOf(listOf(true, true, true)))
        }
    }

    @Test
    fun `should split keys`() {
        val storageEntry = storageEntry(
            StorageEntryType.NMap(
                value = Null,
                keys = listOf(BooleanType, u32, DynamicByteArray("test")),
                hashers = listOf(
                    StorageHasher.Identity,
                    StorageHasher.Blake2_128Concat,
                    StorageHasher.Twox64Concat
                )
            )
        )

        val a1 = true
        val a2 = 123.toBigInteger()
        val a3 = byteArrayOf(1, 2, 3, 4, 5)

        val key = storageEntry.storageKey(runtime, a1, a2, a3)
        val splittedArguments = storageEntry.splitKey(runtime, key)

        assertEquals(a1, splittedArguments[0])
        assertEquals(a2, splittedArguments[1])
        assertArrayEquals(a3, splittedArguments[2] as ByteArray)
    }

    @Test
    fun `should retrieve call by index when indexing is array-like`() {
        val module = newModule(listOf(0, 1, 2))
        val call = module.call(2)

        assertEquals("Function 2", call.name)
    }

    @Test
    fun `should retrieve call by index when indexing is not array-like`() {
        val module = newModule(listOf(1, 2, 3, 5, 8))
        val call = module.call(5)

        assertEquals("Function 5", call.name)
    }

    private fun storageEntry(storageEntryType: StorageEntryType): StorageEntry {
        val mock = Mockito.mock(StorageEntry::class.java)

        Mockito.`when`(mock.type).thenReturn(storageEntryType)
        Mockito.`when`(mock.moduleName).thenReturn(MODULE_NAME)
        Mockito.`when`(mock.name).thenReturn(CALL_NAME)

        return mock
    }

    private fun newModule(callIndices: List<Int>): Module {
        val mock = Mockito.mock(Module::class.java)

        val calls = callIndices.map { newCall(moduleIndex = 0, callIndex = it) }
            .associateBy(MetadataFunction::name)

        Mockito.`when`(mock.calls).thenReturn(calls)

        return mock
    }

    private fun newCall(moduleIndex: Int, callIndex: Int) = MetadataFunction(
        name = "Function $callIndex",
        arguments = emptyList(),
        documentation = emptyList(),
        index = moduleIndex to callIndex
    )
}