@file:UseSerializers(BigIntegerSerializer::class)

package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.junit.Test
import java.math.BigInteger


class StructTest: EncodeTest() {

    @Test
    fun `should encode stuct`() {

        @Serializable
        class Test(val a: BigInteger, val b: String, val c: BigInteger)

        runEncodeTest(
            value = Test(BigInteger.ZERO, "123", BigInteger.TEN),
            expected = mapOf(
                "a" to BigInteger.ZERO,
                "b" to "123",
                "c" to BigInteger.TEN
            )
        )
    }

    @Test
    fun `should encode nested stuct`() {

        @Serializable
        class Inner(
            val b: String,
            val c: BigInteger
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
                    c = BigInteger.ONE
                ),
                d = "321"
            ),
            expected = mapOf(
                "a" to BigInteger.ZERO,
                "inner" to mapOf(
                    "b" to "123",
                    "c" to BigInteger.ONE
                ),
                "d" to "321"
            )
        )
    }
}
