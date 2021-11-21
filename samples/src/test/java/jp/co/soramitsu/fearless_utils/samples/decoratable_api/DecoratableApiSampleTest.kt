package jp.co.soramitsu.fearless_utils.samples.decoratable_api

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DecoratableApiSampleTest {

    @Test
    fun run() = runBlocking {
        DecoratableApiSample().run()
    }
}