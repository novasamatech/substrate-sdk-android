package jp.co.soramitsu.fearless_utils.decoratable_api.const.system

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.const.Constant
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConst
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConstantsModule
import jp.co.soramitsu.fearless_utils.decoratable_api.const.numberConstant
import jp.co.soramitsu.fearless_utils.decoratable_api.moduleNotFound
import java.math.BigInteger

interface SystemConst : DecoratableConstantsModule

val DecoratableConst.systemOrNull: SystemConst?
    get() = decorate("System") {
        object : SystemConst, DecoratableConstantsModule by this {}
    }

val DecoratableConst.system: SystemConst
    get() = systemOrNull ?: SubstrateApiException.moduleNotFound("System")

val SystemConst.blockHashCountOrNull: Constant<BigInteger>?
    get() = decorator.numberConstant("BlockHashCount")
