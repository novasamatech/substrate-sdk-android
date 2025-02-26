package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.BigIntegerSerializable
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test
import java.math.BigInteger


class StructTest : DecodeTest() {

    @Test
    fun `should decode struct`() {

        @Serializable
        data class Test(val a: Int, val b: Boolean)

        runDecodeTest(
            expected = Test(0, false),
            raw = Struct.Instance(
                mapOf(
                    "a" to BigInteger.ZERO,
                    "b" to false
                )
            )
        )
    }

    @Test
    fun `should encode nested struct`() {

        @Serializable
        data class Inner(
            val b: Int,
            val c: Boolean
        )

        @Serializable
        data class Outer(
            val a: BigIntegerSerializable,
            val inner: Inner,
            val d: Int
        )

        runDecodeTest(
            expected = Outer(
                a = BigInteger.ZERO,
                inner = Inner(
                    b = 123,
                    c = true
                ),
                d = 321
            ),
            raw = Struct.Instance(
                mapOf(
                    "a" to BigInteger.ZERO,
                    "inner" to Struct.Instance(
                        mapOf(
                            "b" to 123.toBigInteger(),
                            "c" to true
                        )
                    ),
                    "d" to 321.toBigInteger()
                )
            )
        )
    }
}