package jp.co.soramitsu.fearless_utils.keyring.keypair.ethereum

import jp.co.soramitsu.fearless_utils.keyring.junction.Junction
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.generate

object EthereumKeypairFactory {

    fun generate(seed: ByteArray, junctions: List<Junction>): Keypair {
        return Bip32KeypairFactory.generate(seed, junctions)
    }
}
