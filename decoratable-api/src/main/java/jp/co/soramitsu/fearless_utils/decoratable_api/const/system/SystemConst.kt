package jp.co.soramitsu.fearless_utils.decoratable_api.const.system

import jp.co.soramitsu.fearless_utils.decoratable_api.const.Constant
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConst
import jp.co.soramitsu.fearless_utils.decoratable_api.const.DecoratableConstantsModule
import jp.co.soramitsu.fearless_utils.decoratable_api.const.constant
import java.math.BigInteger

internal interface SystemConst : DecoratableConstantsModule

internal val DecoratableConst.systemOrNull: SystemConst?
    get() = decorate("System") {
        object : SystemConst, DecoratableConstantsModule by this {}
    }

internal val SystemConst.blockHashCountOrNull: Constant<BigInteger>?
    get() = decorator.constant("BlockHashCount")
