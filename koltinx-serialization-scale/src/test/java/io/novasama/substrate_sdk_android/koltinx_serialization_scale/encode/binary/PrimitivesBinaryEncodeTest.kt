package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encodeToByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.scale.dataType.uint16
import io.novasama.substrate_sdk_android.scale.dataType.uint32
import io.novasama.substrate_sdk_android.scale.dataType.uint64
import io.novasama.substrate_sdk_android.scale.dataType.uint8
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray
import kotlinx.serialization.Serializable
import org.junit.Assert
import org.junit.Test

class PrimitivesBinaryEncodeTest : BinaryEncodeTest() {

    @Test
    fun `should encode boolean as element`() {
        @Serializable
        data class TestData(val a: Boolean)

        runEncodeTest(value = TestData(true), expected = byteArrayOf(0x01))
        runEncodeTest(value = TestData(false), expected = byteArrayOf(0x00))
    }

    @Test
    fun `should encode boolean as root`() {
        runEncodeTest(value = true, expected = byteArrayOf(0x01))
        runEncodeTest(value = false, expected = byteArrayOf(0x00))
    }

    @Test
    fun `should encode byte`() {
        runEncodeTest(value = 0x12.toByte(), expected = byteArrayOf(0x12))
    }

    @Test
    fun `should encode long`() {
        val value: Long = 123
        val expected = useScaleWriter { writeLong(value) }
        runEncodeTest(value = value, expected = expected)
    }

    @Test
    fun `should encode short`() {
        val value: Short = 123
        val expected = useScaleWriter { writeShort(value) }
        runEncodeTest(value = value, expected = expected)
    }

    @Test
    fun `should encode int`() {
        val value = 123
        val expected = useScaleWriter { writeUint32(value) }
        runEncodeTest(value = value, expected = expected)
    }

    @Test
    fun `should encode u8`() {
        listOf(UByte.MIN_VALUE, (-1).toUByte(), 0.toUByte(), 1.toUByte(), UByte.MAX_VALUE).forEach {
            val expected = uint8.toByteArray(it)
            runEncodeTest(value = it, expected = expected)
        }
    }

    @Test
    fun `should encode u16`() {
        listOf(UShort.MIN_VALUE, (-1).toUShort(), 0.toUShort(), 1.toUShort(), UShort.MAX_VALUE).forEach {
            val expected = uint16.toByteArray(it.toInt())
            val result = BinaryScale.encodeToByteArray<UShort>(it)
            Assert.assertArrayEquals(expected, result)
        }
    }

    @Test
    fun `should encode u32`() {
        listOf(UInt.MIN_VALUE, (-1).toUInt(), 0.toUInt(), 1.toUInt(), UInt.MAX_VALUE).forEach {
            val expected = uint32.toByteArray(it)
            runEncodeTest(value = it, expected = expected)
        }
    }

    @Test
    fun `should encode u64`() {
        listOf(ULong.MIN_VALUE, (-1).toULong(), 0.toULong(), 1.toULong(), ULong.MAX_VALUE).forEach {
            val expected = uint64.toByteArray(it.toString().toBigInteger())
            val result = BinaryScale.encodeToByteArray<ULong>(it)
            Assert.assertArrayEquals(expected, result)
        }
    }

    @Test
    fun `should encode numbers as element`() {
        @Serializable
        data class TestData(
            val s1: Byte, val s2: Short, val s3: Int, val s4: Long,
            val u1: UByte, val u2: UShort, val u3: UInt, val u4: ULong,
        )

        val value = TestData(
            1, 2, 3, 4,
            5.toUByte(), 6.toUShort(), 7.toUInt(), 8.toULong()
        )
        val expected = useScaleWriter {
            writeByte(1)
            writeShort(2)
            writeUint32(3)
            writeLong(4)

            writeByte(5)
            writeUint16(6)
            writeUint32(7)
            uint64.write(this, 8.toBigInteger())
        }

        runEncodeTest(value, expected)
    }
}