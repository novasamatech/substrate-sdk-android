package io.novasama.substrate_sdk_android.koltinx_serialization_scale

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLength
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.junit.Assert
import org.junit.Test


@Serializable
sealed interface Sealed1<T> {

    @Serializable
    @EnumIndex(0)
    // When Sealed class subclass has List<AnotherSealed> property, the serializer is constructed
    // incorrectly. The issue seems to be related to List in particular. This can be workaround-ed by
    // wrapping list into a value class
    class A<T>(@FixedLength(1) val sealed2: Sealed2List<T>): Sealed1<T>
}

@JvmInline
@Serializable
value class Sealed2List<T>(val value: List<Sealed2<T>>): List<Sealed2<T>> by value

@Serializable
sealed class Sealed2<T> {

    @Serializable
    @EnumIndex(0)
    class A<T>(val value: T): Sealed2<T>()
}

class GenericTest {



    class GenericManager<T>(
        val serializer: KSerializer<T>
    ) {

        fun decode(byteArray: ByteArray): Sealed1<T> {
            val genericSerializer = Sealed1.serializer(serializer)
            return BinaryScale.decodeFromByteArray(genericSerializer, byteArray)
        }

        fun encode(value: Sealed1<T>): ByteArray {
            val genericSerializer = Sealed1.serializer(serializer)
            return BinaryScale.encodeToByteArray(genericSerializer, value)
        }
    }

    @Test
    fun `should work`() {
        val encodedExpected = byteArrayOf(0x00, 0x00, 0x00)

        val manager = GenericManager(Boolean.serializer())
        val encoded = manager.encode(Sealed1.A(Sealed2List(listOf(Sealed2.A(false)))))
        Assert.assertArrayEquals(encodedExpected, encoded)

        manager.decode(encodedExpected)
    }
}