package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary.types

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.types.BSResult
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary.BinaryDecodeTest
import org.junit.Test

class BSResultBinaryDecodeTest : BinaryDecodeTest() {

    @Test
    fun `should decode Ok with Boolean value`() = runDecodeTest<BSResult<Boolean, Boolean>>(
        raw = byteArrayOf(0x00, 0x01),
        expected = BSResult.Ok(true)
    )

    @Test
    fun `should decode Ok with false Boolean value`() = runDecodeTest<BSResult<Boolean, Boolean>>(
        raw = byteArrayOf(0x00, 0x00),
        expected = BSResult.Ok(false)
    )

    @Test
    fun `should decode Err with Boolean error`() = runDecodeTest<BSResult<Boolean, Boolean>>(
        raw = byteArrayOf(0x01, 0x01),
        expected = BSResult.Err(true)
    )

    @Test
    fun `should decode Err with false Boolean error`() = runDecodeTest<BSResult<Boolean, Boolean>>(
        raw = byteArrayOf(0x01, 0x00),
        expected = BSResult.Err(false)
    )

    @Test
    fun `should decode Ok with Int value`() = runDecodeTest<BSResult<Int, Int>>(
        raw = byteArrayOf(0x00, 0x2A, 0x00, 0x00, 0x00),
        expected = BSResult.Ok(42)
    )

    @Test
    fun `should decode Err with Int error`() = runDecodeTest<BSResult<Int, Int>>(
        raw = byteArrayOf(0x01, 0x64, 0x00, 0x00, 0x00),
        expected = BSResult.Err(100)
    )

    @Test
    fun `should decode Ok with String value`() = runDecodeTest<BSResult<String, String>>(
        raw = byteArrayOf(0x00, 0x08, 0x68, 0x69),
        expected = BSResult.Ok("hi")
    )

    @Test
    fun `should decode Err with String error`() = runDecodeTest<BSResult<String, String>>(
        raw = byteArrayOf(0x01, 0x0C, 0x65, 0x72, 0x72),
        expected = BSResult.Err("err")
    )
}