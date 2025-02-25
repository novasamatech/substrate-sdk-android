package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.BigIntegerSerializable
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test
import java.math.BigInteger


class StructTest : EncodeTest() {

    @Test
    fun `should encode stuct`() {

        @Serializable
        class Test(val a: BigIntegerSerializable, val b: Int, val c: Boolean)

        runEncodeTest(
            value = Test(BigInteger.ZERO, 123, false),
            expected = Struct.Instance(
                mapOf(
                    "a" to BigInteger.ZERO,
                    "b" to 123.toBigInteger(),
                    "c" to false
                )
            )
        )
    }

    @Test
    fun `should encode stuct with single field`() {

        @Serializable
        class Test(val a: Int)

        runEncodeTest(
            value = Test(123),
            expected = Struct.Instance(
                mapOf(
                    "a" to 123.toBigInteger(),
                )
            )
        )
    }

    @Test
    fun `should encode nested stuct`() {

        @Serializable
        class Inner(
            val b: Int,
            val c: Boolean
        )

        @Serializable
        class Outer(
            val a: BigIntegerSerializable,
            val inner: Inner,
            val d: Int
        )

        runEncodeTest(
            value = Outer(
                a = BigInteger.ZERO,
                inner = Inner(
                    b = 123,
                    c = true
                ),
                d = 321
            ),
            expected = Struct.Instance(
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