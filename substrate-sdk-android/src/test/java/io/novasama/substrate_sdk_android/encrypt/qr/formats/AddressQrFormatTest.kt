package io.novasama.substrate_sdk_android.encrypt.qr.formats

import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.encrypt.qr.QrFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class AddressQrFormatTest {

    @Test
    fun `should throw on decode if address validation fails`() {
        val format = AddressQrFormat(addressValidator = { false })

        assertThrows<QrFormat.InvalidFormatException> {
            format.decode("test")
        }
    }

    @Test
    fun `should decode if address validation succeeds`() {
        val format = AddressQrFormat(addressValidator = { true })

        assertEquals("test", format.decode("test").address)
    }
}