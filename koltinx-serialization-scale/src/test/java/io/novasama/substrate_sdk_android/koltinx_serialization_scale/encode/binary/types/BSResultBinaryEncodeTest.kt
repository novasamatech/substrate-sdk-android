package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary.types

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.types.BSResult
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary.BinaryEncodeTest
import org.junit.Test

class BSResultBinaryEncodeTest : BinaryEncodeTest() {

    @Test
    fun `should encode Ok with Boolean value`() = runEncodeTest(
        value = BSResult.Ok(true) as BSResult<Boolean, Nothing>,
        expected = byteArrayOf(0x00, 0x01)
    )

    @Test
    fun `should encode Ok with false Boolean value`() = runEncodeTest(
        value = BSResult.Ok(false) as BSResult<Boolean, Nothing>,
        expected = byteArrayOf(0x00, 0x00)
    )

    @Test
    fun `should encode Err with Boolean error`() = runEncodeTest(
        value = BSResult.Err(true) as BSResult<Nothing, Boolean>,
        expected = byteArrayOf(0x01, 0x01)
    )

    @Test
    fun `should encode Err with false Boolean error`() = runEncodeTest(
        value = BSResult.Err(false) as BSResult<Nothing, Boolean>,
        expected = byteArrayOf(0x01, 0x00)
    )

    @Test
    fun `should encode Ok with Int value`() = runEncodeTest(
        value = BSResult.Ok(42) as BSResult<Int, Nothing>,
        expected = byteArrayOf(0x00, 0x2A, 0x00, 0x00, 0x00)
    )

    @Test
    fun `should encode Err with Int error`() = runEncodeTest(
        value = BSResult.Err(100) as BSResult<Nothing, Int>,
        expected = byteArrayOf(0x01, 0x64, 0x00, 0x00, 0x00)
    )

    @Test
    fun `should encode Ok with String value`() = runEncodeTest(
        value = BSResult.Ok("hi") as BSResult<String, Nothing>,
        expected = byteArrayOf(0x00, 0x08, 0x68, 0x69)
    )

    @Test
    fun `should encode Err with String error`() = runEncodeTest(
        value = BSResult.Err("err") as BSResult<Nothing, String>,
        expected = byteArrayOf(0x01, 0x0C, 0x65, 0x72, 0x72)
    )
}