package jp.co.soramitsu.fearless_utils.encrypt.qr.formats

import jp.co.soramitsu.fearless_utils.test_shared.assertThrows
import jp.co.soramitsu.fearless_utils.keyring.qr.QrFormat
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
