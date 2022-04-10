package jp.co.soramitsu.fearless_utils.keyring.mnemonic

import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import org.junit.Assert.*
import org.junit.Test

class MnemonicCreatorTest {

    private val expectedEntropyHex = "2a5ecdeb7466f14d3c06d5aa5c6d433d"
    private val expectedMnemonicWords = "clean wait kiss trip humor pledge useless survey prevent toddler express knock"

    @Test
    fun `should generate mnemonic of different length`() {
        val mnemonic = MnemonicCreator.randomMnemonic(
            Mnemonic.Length.TWELVE)
        assertEquals(12,  mnemonic.wordList.size)

        val mnemonic2 = MnemonicCreator.randomMnemonic(
            Mnemonic.Length.TWENTY_ONE)
        assertEquals(21, mnemonic2.wordList.size)
    }

    @Test
    fun `should generate mnemonic from entropy`() {
       runMnemonicDecodingTest(expectedMnemonicWords)
    }

    @Test
    fun generateMnemonicFromEntropy() {
        val mnemonic = MnemonicCreator.fromEntropy(expectedEntropyHex.fromHex())

        assertEquals(expectedMnemonicWords, mnemonic.words)
    }

    @Test
    fun `should generate mnemonic from input with extra special symbols`() {
        runMnemonicDecodingTest(" clean wait kiss trip humor pledge useless survey prevent toddler express knock \t,")
        runMnemonicDecodingTest("\t clean wait kiss trip humor pledge useless survey prevent toddler express knock")
        runMnemonicDecodingTest("\n clean wait kiss trip humor pledge useless survey prevent toddler express knock")
        runMnemonicDecodingTest("clean, wait\t kiss    trip,    humor\n pledge useless survey prevent toddler express knock")
    }

    private fun runMnemonicDecodingTest(words: String) {
        val mnemonic = MnemonicCreator.fromWords(words)

        assertEquals(expectedEntropyHex, mnemonic.entropy.toHexString())
        assertEquals(expectedMnemonicWords, mnemonic.words)
    }
}
