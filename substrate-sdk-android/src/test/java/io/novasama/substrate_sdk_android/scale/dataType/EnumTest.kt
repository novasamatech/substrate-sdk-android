package io.novasama.substrate_sdk_android.scale.dataType

import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.scale.enum
import io.novasama.substrate_sdk_android.scale.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

enum class TestEnum {
    ZERO, ONE, TWO
}

object EnumTypeTest : Schema<EnumTypeTest>() {
    val enumField by enum(TestEnum::class)
}

@RunWith(MockitoJUnitRunner::class)
class EnumTest {

    @Test
    fun `should serialize an deserialize enum`() {
        val hex = "0x01"

        val parsed = EnumTypeTest.read(hex)

        assertEquals(parsed[EnumTypeTest.enumField], TestEnum.ONE)

        val afterIo = parsed.toHexString()

        assertEquals(hex, afterIo)
    }
}