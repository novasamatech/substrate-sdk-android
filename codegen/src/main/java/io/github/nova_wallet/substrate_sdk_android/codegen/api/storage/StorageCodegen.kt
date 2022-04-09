package io.github.nova_wallet.substrate_sdk_android.codegen.api.storage

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.nova_wallet.substrate_sdk_android.codegen.api.ModuleElementCodegen
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeFormatting.unknownType
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import io.github.nova_wallet.substrate_sdk_android.codegen.common.snakeToLowerCamelCase
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.isFullyResolved
import jp.co.soramitsu.fearless_utils.runtime.metadata.dimension
import jp.co.soramitsu.fearless_utils.runtime.metadata.fullName
import jp.co.soramitsu.fearless_utils.runtime.metadata.keys
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry

private const val DECORATABLE_API_PACKAGE = "jp.co.soramitsu.fearless_utils.decoratable_api.query"

class StorageCodegen(
    private val typeUnfolding: TypeUnfolding,
): ModuleElementCodegen {

    override val fileName: String = "Query"

    override fun FileSpec.Builder.applyModule(module: Module, packageName: String) {
        val decoratableStorageType = ClassName(DECORATABLE_API_PACKAGE, "DecoratableStorage")

        val storageInterface = storageInterface(module.name, decoratableStorageType)
        val storageInterfaceType = interfaceType(module.name, packageName)
        addType(storageInterface)

        val storageProperty = storageProperty(module.name, decoratableStorageType, storageInterfaceType)
        addProperty(storageProperty)

        module.storage?.entries?.values?.forEach { storageEntry ->
            val storageEntryProperty = storageEntryProperty(storageEntry, storageInterfaceType)
            addProperty(storageEntryProperty)
        }
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

    /*
    val StakingStorage.bonded: StorageEntry1<AccountId, AccountId>
    get() = decorator.map1("Bonded")
    */
    private fun storageEntryProperty(
        storageEntry: StorageEntry,
        storageInterfaceType: TypeName,
        ): PropertySpec {
        val getterBlock = """
            return decorator.%decoratorFunction:M(%entryName:S)
            """.trimIndent()

        val getterArgs = mapOf(
            "decoratorFunction" to storageEntryDecoratorFunction(storageEntry),
            "entryName" to storageEntry.name,
        )

        return PropertySpec.builder(name = storageEntry.name.decapitalize(), type = storageEntryReturnType(storageEntry))
            .receiver(storageInterfaceType)
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

    private fun storageEntryDecoratorFunction(storageEntry: StorageEntry): MemberName {
        val functionName = "map${storageEntry.type.dimension()}"

        return MemberName(DECORATABLE_API_PACKAGE, functionName, isExtension = true)
    }

    private fun storageEntryReturnType(storageEntry: StorageEntry): TypeName {
        val arity = storageEntry.type.dimension()

        val entryTypeRaw = ClassName(DECORATABLE_API_PACKAGE, "StorageEntry$arity")

        val argumentTypeNames = (storageEntry.keys + storageEntry.type.value).mapIndexed { index, type ->
            if (!type.isFullyResolved()) unknownType(parent = storageEntry.fullName, child = index.toString())

            typeUnfolding.runtimeTypeToTypeName(type!!, parent = storageEntry.fullName)
        }

        return entryTypeRaw.parameterizedBy(argumentTypeNames)
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
