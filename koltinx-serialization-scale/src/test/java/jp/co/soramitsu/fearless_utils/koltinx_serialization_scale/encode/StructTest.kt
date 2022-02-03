@file:UseSerializers(BigIntegerSerializer::class)

package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.junit.Test
import java.math.BigInteger


class StructTest : EncodeTest() {

    @Test
    fun `should encode stuct`() {

        @Serializable
        class Test(val a: BigInteger, val b: String, val c: BigInteger)

        runEncodeTest(
            value = Test(BigInteger.ZERO, "123", BigInteger.TEN),
            expected = Struct.Instance(
                mapOf(
                    "a" to BigInteger.ZERO,
                    "b" to "123",
                    "c" to BigInteger.TEN
                )
            )
        )
    }

    @Test
    fun `should encode stuct with single field`() {

        @Serializable
        class Test(val a: String)

        runEncodeTest(
            value = Test("123"),
            expected = Struct.Instance(
                mapOf(
                    "a" to "123",
                )
            )
        )
    }

    @Test
    fun `should encode nested stuct`() {

        @Serializable
        class Inner(
            val b: String,
            val c: Boolean
        )

        @Serializable
        class Outer(
            val a: BigInteger,
            val inner: Inner,
            val d: String
        )

        runEncodeTest(
            value = Outer(
                a = BigInteger.ZERO,
                inner = Inner(
                    b = "123",
                    c = true
                ),
                d = "321"
            ),
            expected = Struct.Instance(
                mapOf(
                    "a" to BigInteger.ZERO,
                    "inner" to Struct.Instance(
                        mapOf(
                            "b" to "123",
                            "c" to true
                        )
                    ),
                    "d" to "321"
                )
            )
        )
    }
}
