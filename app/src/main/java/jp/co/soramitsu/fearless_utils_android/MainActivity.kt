package jp.co.soramitsu.fearless_utils_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate.SubstrateKeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.seed.SeedFactory
import jp.co.soramitsu.fearless_utils.encrypt.seed.ethereum.EthereumSeedFactory
import jp.co.soramitsu.fearless_utils.encrypt.seed.substrate.SubstrateSeedFactory
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var test_account_mnemonic = "nerve gesture jealous wealth rally priority apple visual mom boil evoke six"
        val seed = deriveSeed(test_account_mnemonic,null,false)
        val pairs =  SubstrateKeypairFactory.generate(EncryptionType.SR25519, seed.seed, arrayListOf())
        Log.e("test","Public Key: ${pairs.publicKey} , Private Key: ${pairs.privateKey}")
    }

    private fun deriveSeed(mnemonic: String, password: String?, ethereum: Boolean): SeedFactory.Result {
        return if (ethereum) {
            EthereumSeedFactory.deriveSeed(mnemonic, password)
        } else {
            SubstrateSeedFactory.deriveSeed32(mnemonic, password)
        }
    }
    fun SeedFactory.deriveSeed32(mnemonicWords: String, password: String?) = cropSeedTo32Bytes(deriveSeed(mnemonicWords, password))
    private fun cropSeedTo32Bytes(seedResult: SeedFactory.Result): SeedFactory.Result {
        return SeedFactory.Result(seed = seedResult.seed.copyOfRange(0, 32), seedResult.mnemonic)
    }
}
