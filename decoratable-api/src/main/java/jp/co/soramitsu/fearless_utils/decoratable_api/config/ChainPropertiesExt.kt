package jp.co.soramitsu.fearless_utils.decoratable_api.config

import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.ss58Address
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAddress

fun ChainProperties.ss58AddressOf(publicKey: ByteArray) = publicKey.toAddress(ss58Format)

fun ChainProperties.ss58AddressOf(keypair: Keypair) = keypair.ss58Address(ss58Format)
