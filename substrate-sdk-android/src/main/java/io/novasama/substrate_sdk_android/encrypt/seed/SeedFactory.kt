package io.novasama.substrate_sdk_android.encrypt.seed

import io.novasama.substrate_sdk_android.encrypt.mnemonic.Mnemonic

interface SeedFactory {

    class Result(val seed: ByteArray, val mnemonic: Mnemonic)

    fun createSeed(length: Mnemonic.Length, password: String?): Result

    fun deriveSeed(mnemonicWords: String, password: String?): Result
}
