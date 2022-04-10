package jp.co.soramitsu.fearless_utils.decoratable_api.const.babe

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.const.Constant
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConst
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConstantsModule
import jp.co.soramitsu.fearless_utils.decoratable_api.const.constant
import jp.co.soramitsu.fearless_utils.decoratable_api.constantNotFound
import java.math.BigInteger

internal interface BabeConst : DecoratableConstantsModule

internal val DecoratableConst.babeOrNull: BabeConst?
    get() = decorate("Babe") {
        object : BabeConst, DecoratableConstantsModule by this {}
    }

internal val BabeConst.expectedBlockTimeOrNull: Constant<BigInteger>?
    get() = decorator.constant("ExpectedBlockTime")

internal val BabeConst.expectedBlockTime: Constant<BigInteger>
    get() = expectedBlockTimeOrNull ?: SubstrateApiException.constantNotFound("ExpectedBlockTime")
