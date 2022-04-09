package io.github.nova_wallet.substrate_sdk_android.codegen.api

import com.squareup.kotlinpoet.FileSpec
import io.github.nova_wallet.substrate_sdk_android.codegen.api.storage.StorageCodegen
import io.github.nova_wallet.substrate_sdk_android.codegen.api.tx.TxCodegen
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import org.slf4j.Logger
import java.io.File

interface ModuleElementCodegen {

    val fileName: String

    fun FileSpec.Builder.applyModule(module: Module, packageName: String)
}

class ModuleCodegen(
    logger: Logger,
    private val parentDirectory: File,
    typeUnfolding: TypeUnfolding,
    private val elementCodegens: List<ModuleElementCodegen> = listOf(
        StorageCodegen(typeUnfolding),
        TxCodegen(logger, typeUnfolding)
    )
) {

    fun generate(module: Module) {
        val packageName = module.name.decapitalize()

        elementCodegens.forEach { moduleElementCodegen ->
            FileSpec.builder(packageName, moduleElementCodegen.fileName)
                .also { builder -> with(moduleElementCodegen) { builder.applyModule(module, packageName) } }
                .build()
                .writeTo(parentDirectory)
        }
    }
}
