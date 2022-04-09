package jp.co.soramitsu.fearless_utils.decoratable_api.const.timestamp

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.const.Constant
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConst
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConstantsModule
import jp.co.soramitsu.fearless_utils.decoratable_api.const.constant
import jp.co.soramitsu.fearless_utils.decoratable_api.constantNotFound
import java.math.BigInteger

internal interface TimestampConst : DecoratableConstantsModule

internal val DecoratableConst.timestampOrNull: TimestampConst?
    get() = decorate("Timestamp") {
        object : TimestampConst, DecoratableConstantsModule by this {}
    }

internal val TimestampConst.minimumPeriodOrNull: Constant<BigInteger>?
    get() = decorator.constant("MinimumPeriod")
