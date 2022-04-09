package io.github.nova_wallet.substrate_sdk_android.codegen.api

import com.squareup.kotlinpoet.FileSpec
import io.github.nova_wallet.substrate_sdk_android.codegen.api.storage.StorageCodegen
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import java.io.File

interface ModuleElementCodegen {

    val fileName: String

    fun FileSpec.Builder.applyModule(module: Module, packageName: String)
}

class ModuleCodegen(
    private val parentDirectory: File,
    private val elementCodegens: List<ModuleElementCodegen> = listOf(
        StorageCodegen()
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
