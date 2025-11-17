package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.BigIntegerSerializable
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray
import kotlinx.serialization.Serializable
import org.junit.Test
import java.math.BigInteger

class CompactBinaryEncodeTest : BinaryEncodeTest() {

    @Test
    fun `should encode compact`() {
        val number = 100.toBigInteger()
        runEncodeTest(number, encodedOf(number))
    }

    @Test
    fun `should encode compact as element`() {
        @Serializable
        data class TestData(val a: BigIntegerSerializable)

        val number = 100.toBigInteger()
        runEncodeTest(TestData(number), encodedOf(number))
    }

    private fun encodedOf(compact: BigInteger): ByteArray {
        return compactInt.toByteArray(compact)
    }
}