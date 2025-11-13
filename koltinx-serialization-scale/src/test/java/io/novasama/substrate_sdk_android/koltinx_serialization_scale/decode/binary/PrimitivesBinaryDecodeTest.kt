package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decodeFromByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.scale.dataType.list
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray
import io.novasama.substrate_sdk_android.scale.dataType.uint16
import io.novasama.substrate_sdk_android.scale.dataType.uint32
import io.novasama.substrate_sdk_android.scale.dataType.uint64
import io.novasama.substrate_sdk_android.scale.dataType.uint8
import kotlinx.serialization.Serializable
import org.junit.Assert
import org.junit.Test

class PrimitivesBinaryDecodeTest : BinaryDecodeTest() {

    @Test
    fun `should decode boolean as element`() {
        @Serializable
        data class TestData(val a: Boolean)

        runDecodeTest(raw = byteArrayOf(0x01), expected = TestData(true))
        runDecodeTest(raw = byteArrayOf(0x00), expected = TestData(false))
    }

    @Test
    fun `should decode boolean as root`() {
        runDecodeTest(raw = byteArrayOf(0x01), expected = true)
        runDecodeTest(raw = byteArrayOf(0x00), expected = false)
    }

    @Test
    fun `should decode byte`() {
        runDecodeTest(raw = byteArrayOf(0x12), expected = 0x12.toByte())
    }

    @Test
    fun `should decode long`() {
        val expected: Long = 123
        val encoded = useScaleWriter { writeLong(expected) }
        runDecodeTest(raw = encoded, expected = expected)
    }

    @Test
    fun `should decode short`() {
        val expected: Short = 123
        val encoded = useScaleWriter { writeShort(expected) }
        runDecodeTest(raw = encoded, expected = expected)
    }

    @Test
    fun `should decode byte as element`() {
        @Serializable
        data class TestData(val a: Byte)

        runDecodeTest(raw = byteArrayOf(0x12), expected = TestData(0x12))
    }

    @Test
    fun `should decode u8`() {
        listOf(UByte.MIN_VALUE, (-1).toUByte(), 0.toUByte(), 1.toUByte(), UByte.MAX_VALUE).forEach {
            val raw = uint8.toByteArray(it)
            runDecodeTest(raw = raw, expected = it)
        }
    }

    @Test
    fun `should decode u16`() {
        listOf(UShort.MIN_VALUE, (-1).toUShort(), 0.toUShort(), 1.toUShort(), UShort.MAX_VALUE).forEach {
            val raw = uint16.toByteArray(it.toInt())
            val result = BinaryScale.decodeFromByteArray<UShort>(raw)
            Assert.assertEquals(it, result)
        }
    }

    @Test
    fun `should decode u32`() {
        listOf(UInt.MIN_VALUE, (-1).toUInt(), 0.toUInt(), 1.toUInt(), UInt.MAX_VALUE).forEach {
            val raw = uint32.toByteArray(it)
            runDecodeTest(raw = raw, expected = it)
        }
    }

    @Test
    fun `should decode u64`() {
        listOf(ULong.MIN_VALUE, (-1).toULong(), 0.toULong(), 1.toULong(), ULong.MAX_VALUE).forEach {
            val raw = uint64.toByteArray(it.toString().toBigInteger())
            val result = BinaryScale.decodeFromByteArray<ULong>(raw)
            Assert.assertEquals(it, result)
        }
    }
}