package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.TransientStruct
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.BigIntegerSerializable
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.SerialName
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

    @Test
    fun `SerialName works`() {

        @Serializable
        data class A(@SerialName("b") val a: Boolean)

        runDecodeTest(
            raw = Struct.Instance(mapOf("b" to true)),
            expected = A(a = true)
        )
    }

    @Test
    fun `should decode snake case as camel case`() {

        @Serializable
        data class A(val someName: Boolean)

        runDecodeTest(
            raw = Struct.Instance(mapOf("some_name" to true)),
            expected = A(someName = true)
        )
    }

    @Test
    fun `should decode transient struct`() {

        @Serializable
        @TransientStruct
        data class A(val a: Boolean)

        runDecodeTest(
            expected = A(a = true),
            raw = true
        )
    }

    @Test
    fun `should decode partially defined struct`() {

        @Serializable
        data class A(val a: Boolean)

        runDecodeTest(
            expected = A(a = true),
            raw = Struct.Instance(mapOf("a" to true, "b" to false))
        )
    }
}