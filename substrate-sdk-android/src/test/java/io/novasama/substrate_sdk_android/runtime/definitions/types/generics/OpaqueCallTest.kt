package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.types.BaseTypeTest
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import io.novasama.substrate_sdk_android.runtime.metadata.call
import io.novasama.substrate_sdk_android.runtime.metadata.module
import org.junit.Assert.*
import org.junit.Test

class OpaqueCallTest : BaseTypeTest() {
    val inHex = "0x1001000103"

    val module = runtime.metadata.module("A")
    val function = module.call("B")

    val instance = GenericCall.Instance(
        module = module,
        function = function,
        arguments = mapOf(
            "arg1" to true,
            "arg2" to 3.toBigInteger()
        )
    )

    @Test
    fun `should decode call`() {
        val decoded = OpaqueCall.fromHex(runtime, inHex)

        assertEquals(instance.arguments, decoded.arguments)
        assertEquals(instance.function, decoded.function)
    }

    @Test
    fun `should encode call`() {
        val encoded = OpaqueCall.toHex(runtime, instance)

        assertEquals(inHex, encoded)
    }
}