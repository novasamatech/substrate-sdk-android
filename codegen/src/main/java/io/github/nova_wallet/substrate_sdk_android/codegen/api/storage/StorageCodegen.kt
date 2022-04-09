package io.github.nova_wallet.substrate_sdk_android.codegen.api.storage

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.nova_wallet.substrate_sdk_android.codegen.api.BaseModuleElementCodegen
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeFormatting.unknownType
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.isFullyResolved
import jp.co.soramitsu.fearless_utils.runtime.metadata.dimension
import jp.co.soramitsu.fearless_utils.runtime.metadata.fullName
import jp.co.soramitsu.fearless_utils.runtime.metadata.keys
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry

class StorageCodegen(
    typeUnfolding: TypeUnfolding,
) : BaseModuleElementCodegen(
    elementSubPackage = "query",
    sectionInterfaceName = "DecoratableQuery",
    parentInterfaceName = "DecoratableStorage",
    fileName = "Query",
    typeUnfolding = typeUnfolding
) {

    override fun FileSpec.Builder.generateChildInterfaceExtensions(module: Module, childInterfaceType: TypeName) {
        module.storage?.entries?.values?.forEach { storageEntry ->
            val storageEntryProperty = storageEntryProperty(storageEntry, childInterfaceType)
            addProperty(storageEntryProperty)
        }
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

        return MemberName(elementApiPackage, functionName, isExtension = true)
    }

    private fun storageEntryReturnType(storageEntry: StorageEntry): TypeName {
        val arity = storageEntry.type.dimension()

        val entryTypeRaw = ClassName(elementApiPackage, "StorageEntry$arity")

        val argumentTypeNames = (storageEntry.keys + storageEntry.type.value).mapIndexed { index, type ->
            if (!type.isFullyResolved()) unknownType(parent = storageEntry.fullName, child = index.toString())

            type!!.toTypeName(storageEntry.fullName)
        }

        return entryTypeRaw.parameterizedBy(argumentTypeNames)
    }
}
