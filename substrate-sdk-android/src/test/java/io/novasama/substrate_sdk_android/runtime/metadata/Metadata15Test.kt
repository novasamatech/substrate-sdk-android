package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.metadata.MetadataTestCommon.buildPost14TestRuntime
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAccountId
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class Metadata15Test {

    @Test
    fun `should decode and use metadata v15`() {
       val runtime = buildPost14TestRuntime("metadata_polkadot_v15")

        testRuntimeApis(runtime)
    }

    private fun testRuntimeApis(runtime: RuntimeSnapshot) {
        val method = runtime.metadata.runtimeApi("StakingApi").method("eras_stakers_page_count")

        val arguments = mapOf(
            "era" to BigInteger.ZERO,
            "account" to "16PXa3vGfsMvBddhxEn3S5waujJBjvfEahjTg7TyTJ5cHNd7".toAccountId()
        )

        val request = method.createRequest(runtime, arguments)

        val (paramName, paramArgs) = request.params

        assertEquals("StakingApi_eras_stakers_page_count", paramName)
        assertEquals("0x00000000ee5b79303b8da6bf7e77fc735eb2f02d87d685281e7e81624ed3b54d3ccfe865", paramArgs)

        val result = "0x01000000"
        val decodedResult = method.decodeOutput(runtime, result)

        assertEquals(BigInteger.ONE, decodedResult)
    }
}