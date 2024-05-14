package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.runtime.metadata.MetadataTestCommon.buildPost14TestRuntime
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class Metadata15Test {

    @Test
    fun `should decode metadata v15`() {
       val runtime = buildPost14TestRuntime("metadata_polkadot_v15")
    }
}