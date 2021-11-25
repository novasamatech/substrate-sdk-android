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
}
