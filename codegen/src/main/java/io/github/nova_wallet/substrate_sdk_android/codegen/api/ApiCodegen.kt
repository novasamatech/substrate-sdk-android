package io.github.nova_wallet.substrate_sdk_android.codegen.api

import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import java.io.File

class ApiCodegen(parentDirectory: File, typeUnfolding: TypeUnfolding) {

    private val apiDirectory = File(parentDirectory, "api")
    private val moduleCodegen = ModuleCodegen(apiDirectory, typeUnfolding)

    fun generate(runtime: RuntimeSnapshot) {
        runtime.metadata.modules.values.forEach {
            moduleCodegen.generate(it)
        }
    }
}
