package jp.co.soramitsu.fearless_utils.keyring

import net.i2p.crypto.eddsa.EdDSASecurityProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

object SecurityProviders {

    val requireEdDSA by lazy {
        Security.addProvider(EdDSASecurityProvider())
    }

    val requireBouncyCastle by lazy {
        Security.addProvider(BouncyCastleProvider())
    }
}
