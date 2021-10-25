package jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereum

import jp.co.soramitsu.fearless_utils.encrypt.junction.Junction
import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.generate

object EthereumKeypairFactory {

    fun generate(seed: ByteArray, junctions: List<Junction>): Keypair {
        return Bip32KeypairFactory.generate(seed, junctions)
    }
}
