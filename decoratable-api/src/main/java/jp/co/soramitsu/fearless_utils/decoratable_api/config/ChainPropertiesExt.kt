package jp.co.soramitsu.fearless_utils.decoratable_api.config

import jp.co.soramitsu.fearless_utils.keyring.adress.SubstrateAccountId
import jp.co.soramitsu.fearless_utils.keyring.adress.toAddress

fun ChainProperties.ss58AddressOf(accountId: SubstrateAccountId) = accountId.toAddress(ss58Format)
