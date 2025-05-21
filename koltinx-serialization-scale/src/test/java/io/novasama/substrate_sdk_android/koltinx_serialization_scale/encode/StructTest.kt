package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.AsTuple
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.TransientStruct
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.BigIntegerSerializable
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.SerialName
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

    @Test
    fun `SerialName works`() {

        @Serializable
        data class A(@SerialName("b") val a: Boolean)

        runEncodeTest(
            expected = Struct.Instance(mapOf("b" to true)),
            value = A(a = true)
        )
    }

    @Test
    fun `should encode camel case as snake case`() {
        @Serializable
        data class A(val aB: Boolean)

        runEncodeTest(
            expected = Struct.Instance(mapOf("aB" to true, "a_b" to true)),
            value = A(aB = true)
        )
    }

    @Test
    fun `should encode transient struct`() {

        @Serializable
        @TransientStruct
        data class A(val a: Boolean)

        runEncodeTest(
            expected = true,
            value = A(a = true)
        )
    }

    @Test
    fun `should encode as tuple struct`() {

        @Serializable
        @AsTuple
        data class A(val a: Boolean, val b: Boolean)

        runEncodeTest(
            expected = listOf(true, false),
            value = A(a = true, b = false)
        )
    }
}