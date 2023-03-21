package io.novasama.substrate_sdk_android.wsrpc.subscription.response

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SubscriptionChangeTest {

    private val gson = Gson()

    @Test
    fun `should handle string subscription id`() = runSubscriptionIdTest(
        id = "1lQdo0fUlXLJS9v3", expected = "1lQdo0fUlXLJS9v3"
    )

    @Test
    fun `should handle integer subscription id`() = runSubscriptionIdTest(
        id = 2595588254652828, expected = "2595588254652828"
    )

    private fun runSubscriptionIdTest(id: Any, expected: String) {
        val raw = "{\"jsonrpc\":\"2.0\",\"method\":\"state_storage\",\"params\":{\"subscription\":$id, \"result\":{\"block\":\"0x1deed482cbed9cec67398ca222d073f99e36c48aeba7ebfb418135f7a81c9c87\",\"changes\":[[\"0x26aa394eea5630e07c48ae0c9558cef7b99d880ec681799c0cf30e8886371da9b82e146c42d93bbe2c219bbdfaf698482a1e637d38ab3279321e34f878cabc1fd411dd40d5fde3482601275ef189663c\",\"0x01000000000000007ae384e5ae0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\"]]}}}"
        val subscriptionChange = gson.fromJson(raw, SubscriptionChange::class.java)

        assertEquals(expected, subscriptionChange.subscriptionId)
    }
}