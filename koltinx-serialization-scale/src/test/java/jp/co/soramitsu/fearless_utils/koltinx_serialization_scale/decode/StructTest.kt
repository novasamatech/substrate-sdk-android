@file:UseSerializers(BigIntegerSerializer::class)

package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.junit.Test
import java.math.BigInteger


class StructTest : DecodeTest() {

    @Test
    fun `should decode struct`() {

        @Serializable
        data class Test(val a: BigInteger, val b: String, val c: BigInteger)

        runtDecodeTest(
            expected = Test(BigInteger.ZERO, "123", BigInteger.TEN),
            raw = Struct.Instance(
                mapOf(
                    "a" to BigInteger.ZERO,
                    "b" to "123",
                    "c" to BigInteger.TEN
                )
            )
        )
    }

    @Test
    fun `should encode nested struct`() {

        @Serializable
        data class Inner(
            val b: String,
            val c: Boolean
        )

        @Serializable
        data class Outer(
            val a: BigInteger,
            val inner: Inner,
            val d: String
        )

        runtDecodeTest(
            expected = Outer(
                a = BigInteger.ZERO,
                inner = Inner(
                    b = "123",
                    c = true
                ),
                d = "321"
            ),
            raw = Struct.Instance(
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
