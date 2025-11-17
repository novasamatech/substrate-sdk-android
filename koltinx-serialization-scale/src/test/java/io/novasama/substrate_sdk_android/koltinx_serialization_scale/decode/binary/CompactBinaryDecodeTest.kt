package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.BigIntegerSerializable
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray
import kotlinx.serialization.Serializable
import org.junit.Test
import java.math.BigInteger

class CompactBinaryDecodeTest : BinaryDecodeTest() {

    @Test
    fun `should decode compact`() {
        val number = 100.toBigInteger()
        runDecodeTest(encodedOf(number), number)
    }

    @Test
    fun `should decode compact as element`() {
        @Serializable
        data class TestData(val a: BigIntegerSerializable)

        val number = 100.toBigInteger()
        runDecodeTest(encodedOf(number), TestData(number))
    }

    private fun encodedOf(compact: BigInteger): ByteArray {
        return compactInt.toByteArray(compact)
    }
}