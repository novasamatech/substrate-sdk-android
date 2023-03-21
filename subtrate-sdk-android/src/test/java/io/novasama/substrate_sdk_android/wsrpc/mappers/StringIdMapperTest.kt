package io.novasama.substrate_sdk_android.wsrpc.mappers

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.wsrpc.response.RpcResponse
import org.junit.Assert.*
import org.junit.Test

class StringIdMapperTest {

    val gson = Gson()

    @Test
    fun `should map string id`() = runTest("n6in3VIm96u3ABQE", "n6in3VIm96u3ABQE")

    @Test
    fun `should map int id`() = runTest(2417711256031439, "2417711256031439")

    @Test
    fun `should map double id`() = runTest(2417711256031439.0, "2417711256031439")

    private fun runTest(
        id: Any?,
        expectedResult: String
    ) {
        val response = RpcResponse("2.0", result = id, id=123, error = null)

        val mapper = StringIdMapper.nonNull()

        assertEquals(expectedResult, mapper.map(response, gson))
    }
}