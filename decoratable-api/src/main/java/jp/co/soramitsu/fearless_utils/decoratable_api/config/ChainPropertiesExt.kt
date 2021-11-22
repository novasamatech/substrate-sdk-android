package jp.co.soramitsu.fearless_utils.decoratable_api.config

import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAddress

fun ChainProperties.addressOf(publicKey: ByteArray) = publicKey.toAddress(ss58Format)
