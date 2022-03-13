package io.github.nova_wallet.substrate_sdk_android.codegen

import jp.co.soramitsu.fearless_utils.hash.Hasher.xxHash128

class Codegen {

    fun test() {
        print("123".encodeToByteArray().xxHash128())
    }
}
