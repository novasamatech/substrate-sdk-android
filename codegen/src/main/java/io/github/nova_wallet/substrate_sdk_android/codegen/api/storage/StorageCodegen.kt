package io.github.nova_wallet.substrate_sdk_android.codegen.api.storage

import com.squareup.kotlinpoet.*
import io.github.nova_wallet.substrate_sdk_android.codegen.api.ModuleElementCodegen
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module

private const val DECORATABLE_API_PACKAGE = "jp.co.soramitsu.fearless_utils.decoratable_api.query"

class StorageCodegen : ModuleElementCodegen {

    override val fileName: String = "Query"

    override fun FileSpec.Builder.applyModule(module: Module, packageName: String) {
        val decoratableStorageType = ClassName(DECORATABLE_API_PACKAGE, "DecoratableStorage")

        val storageInterface = storageInterface(module.name, decoratableStorageType)
        val storageInterfaceType = interfaceType(module.name, packageName)
        addType(storageInterface)

        val storageProperty = storageProperty(module.name, decoratableStorageType, storageInterfaceType)
        addProperty(storageProperty)
    }

    /*
    val DecoratableQuery.staking: StakingStorage
    get() = decorate("Staking") {
        object : StakingStorage, DecoratableStorage by this {}
    }
    */
    private fun storageProperty(
        moduleName: String,
        decoratableStorageType: TypeName,
        storageInterfaceType: TypeName,
    ): PropertySpec {
        val decoratableQueryType = ClassName(DECORATABLE_API_PACKAGE, "DecoratableQuery")

        val getterBlock = """
            return decorate(%moduleName:S) {
                object : %interface:T, %parentInterface:T by this {}
            }
            """.trimIndent()

        val getterArgs = mapOf(
            "moduleName" to moduleName,
            "interface" to storageInterfaceType,
            "parentInterface" to decoratableStorageType
        )

        return PropertySpec.builder(moduleName.decapitalize(), storageInterfaceType)
            .receiver(decoratableQueryType)
            .getter(
                FunSpec.getterBuilder()
                    .addCode(
                        CodeBlock.builder()
                            .addNamed(getterBlock, getterArgs)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    // interface StakingStorage : DecoratableStorage
    private fun storageInterface(moduleName: String, decoratableStorageType: TypeName): TypeSpec {
        return TypeSpec.interfaceBuilder(interfaceName(moduleName))
            .addSuperinterface(decoratableStorageType)
            .build()
    }

    private fun interfaceType(moduleName: String, packageName: String) = ClassName(packageName, interfaceName(moduleName))

    private fun interfaceName(moduleName: String) = "${moduleName.capitalize()}Storage"
}
