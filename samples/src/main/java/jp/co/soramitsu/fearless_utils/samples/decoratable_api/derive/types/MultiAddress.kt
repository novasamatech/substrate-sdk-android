package jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.types

import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.MULTI_ADDRESS_ID

typealias MultiAddress = Any

fun AccountId.asMultiAddress() = DictEnum.Entry(MULTI_ADDRESS_ID, this)
