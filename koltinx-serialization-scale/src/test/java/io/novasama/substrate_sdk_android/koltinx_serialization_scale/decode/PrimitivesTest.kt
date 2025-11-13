package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class PrimitivesTest : DecodeTest() {

    @Test
    fun `should decode numbers`() {
        val source = 123.toBigInteger()

        runDecodeTest(source, 123)
        runDecodeTest(source, 123.toByte())
        runDecodeTest(source, 123.toShort())
        runDecodeTest(source, 123.toLong())
        runDecodeTest(source, 123.toBigInteger())
    }

    @Test
    fun `should decode string`() = runDecodeTest(
        raw = "123".encodeToByteArray(),
        expected = "123"
    )

    @Test
    fun `should decode boolean`() = runDecodeTest(
        raw = true,
        expected = true
    )
}