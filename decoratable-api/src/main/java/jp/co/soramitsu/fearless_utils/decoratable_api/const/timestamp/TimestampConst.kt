package jp.co.soramitsu.fearless_utils.decoratable_api.const.timestamp

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConst
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConstantsModule
import jp.co.soramitsu.fearless_utils.decoratable_api.const.numberConstant
import jp.co.soramitsu.fearless_utils.decoratable_api.constantNotFound
import jp.co.soramitsu.fearless_utils.decoratable_api.moduleNotFound

interface TimestampConst : DecoratableConstantsModule

val DecoratableConst.timestampOrNull: TimestampConst?
    get() = decorate("Timestamp") {
        object : TimestampConst, DecoratableConstantsModule by this {}
    }

val DecoratableConst.timestamp: TimestampConst
    get() = timestampOrNull ?: SubstrateApiException.moduleNotFound("Timestamp")

val TimestampConst.minimumPeriodOrNull
    get() = decorator.numberConstant("MinimumPeriod")

val TimestampConst.minimumPeriod
    get() =  minimumPeriodOrNull ?: SubstrateApiException.constantNotFound("MinimumPeriod")
