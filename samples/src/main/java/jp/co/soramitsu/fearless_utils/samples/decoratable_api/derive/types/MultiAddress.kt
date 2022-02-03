@file:UseSerializers(BigIntegerSerializer::class)

package jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.types

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigInteger

@Serializable
sealed class MultiAddress {

    @Serializable
    class Id(val value: ByteArray) : MultiAddress()

    @Serializable
    class Index(val value: BigInteger) : MultiAddress()

    @Serializable
    class Raw(val value: ByteArray) : MultiAddress()

    @Serializable
    class Address32(val value: ByteArray) : MultiAddress() {
        init {
            require(value.size == 32) {
                "Address32 should be 32 bytes long"
            }
        }
    }

    @Serializable
    class Address20(val value: ByteArray) : MultiAddress() {
        init {
            require(value.size == 20) {
                "Address20 should be 20 bytes long"
            }
        }
    }
}
