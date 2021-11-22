package jp.co.soramitsu.fearless_utils.decoratable_api.const.babe

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.const.Constant
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConst
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConstantsModule
import jp.co.soramitsu.fearless_utils.decoratable_api.const.numberConstant
import jp.co.soramitsu.fearless_utils.decoratable_api.constantNotFound
import jp.co.soramitsu.fearless_utils.decoratable_api.moduleNotFound
import java.math.BigInteger

interface BabeConst : DecoratableConstantsModule

val DecoratableConst.babeOrNull: BabeConst?
    get() = decorate("Babe") {
        object : BabeConst, DecoratableConstantsModule by this {}
    }

val DecoratableConst.babe: BabeConst
    get() = babeOrNull ?: SubstrateApiException.moduleNotFound("Babe")

val BabeConst.expectedBlockTimeOrNull: Constant<BigInteger>?
    get() = decorator.numberConstant("ExpectedBlockTime")

val BabeConst.expectedBlockTime: Constant<BigInteger>
    get() = expectedBlockTimeOrNull ?: SubstrateApiException.constantNotFound("ExpectedBlockTime")
