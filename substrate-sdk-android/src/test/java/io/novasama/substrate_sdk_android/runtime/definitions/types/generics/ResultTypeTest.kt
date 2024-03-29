package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.types.BaseTypeTest
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import org.junit.Assert
import org.junit.Test

class ResultTypeTest : BaseTypeTest() {

    @Test
    fun `should decode err false`() {
        val hex = "0x0100"
        val type = ResultType(TypeReference(u32), TypeReference(BooleanType))
        val decoded = type.fromHex(runtime, hex)

        Assert.assertEquals(ResultType.Err, decoded.name)
        Assert.assertEquals(false, decoded.value)
    }

    @Test
    fun `should decode ok u8`() {
        val hex = "0x002a"
        val type = ResultType(TypeReference(u8), TypeReference(BooleanType))
        val decoded = type.fromHex(runtime, hex)

        Assert.assertEquals(ResultType.Ok, decoded.name)
        Assert.assertEquals(42.toBigInteger(), decoded.value)
    }

    @Test
    fun `should encode ok u8`() {
        val type = ResultType(TypeReference(u8), TypeReference(BooleanType))
        val decoded = type.toHex(runtime, DictEnum.Entry(ResultType.Ok, 42.toBigInteger()))

        Assert.assertEquals("0x002a", decoded)
    }

}