package io.github.nova_wallet.substrate_sdk_android.codegen.api

import com.squareup.kotlinpoet.*
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import io.github.nova_wallet.substrate_sdk_android.codegen.common.asNullable
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module

private const val DECORATABLE_API_PACKAGE = "jp.co.soramitsu.fearless_utils.decoratable_api"

abstract class BaseModuleElementCodegen(
    elementSubPackage: String,
    private val sectionInterfaceName: String,
    private val parentInterfaceName: String,
    override val fileName: String,
    private val typeUnfolding: TypeUnfolding,
) : ModuleElementCodegen {

    protected val elementApiPackage = "$DECORATABLE_API_PACKAGE.$elementSubPackage"

    override fun FileSpec.Builder.applyModule(module: Module, packageName: String) {
        val sectionInterfaceType = ClassName(elementApiPackage, sectionInterfaceName)
        val parentInterfaceType = ClassName(elementApiPackage, parentInterfaceName)

        val childInterface = elementInterface(module.name, parentInterfaceType)
        val childInterfaceType = interfaceType(module.name, packageName)
        addType(childInterface)

        val sectionExtensionNullable = sectionExtensionPropertyNullable(module.name, parentInterfaceType, childInterfaceType, sectionInterfaceType)
        addProperty(sectionExtensionNullable)

        val sectionExtensionNonNull = sectionExtensionNameNonNull(module.name, childInterfaceType, sectionInterfaceType)
        addProperty(sectionExtensionNonNull)

        generateChildInterfaceExtensions(module, childInterfaceType)
    }

    abstract fun FileSpec.Builder.generateChildInterfaceExtensions(
        module: Module,
        childInterfaceType: TypeName
    )

    protected fun RuntimeType<*, *>.toTypeName(parentType: String) = typeUnfolding.runtimeTypeToTypeName(this, parentType)

    protected fun elementName(base: String, nullable: Boolean) = if (nullable) {
        base + "OrNull"
    } else {
        base
    }

    /*
    val DecoratableQuery.stakingOrNull: StakingStorage?
    get() = decorate("Staking") {
        object : StakingStorage, DecoratableStorage by this {}
    }
    */
    private fun sectionExtensionPropertyNullable(
        moduleName: String,
        parentInterfaceType: TypeName,
        childInterfaceType: TypeName,
        sectionInterfaceType: TypeName
    ): PropertySpec {
        val getterBlock = """
            return decorate(%moduleName:S) {
                object : %childInterface:T, %parentInterface:T by this {}
            }
        """.trimIndent()

        val getterArgs = mapOf(
            "moduleName" to moduleName,
            "childInterface" to childInterfaceType,
            "parentInterface" to parentInterfaceType
        )

        return PropertySpec.builder(
            name = sectionExtensionName(moduleName, nullable = true),
            type = childInterfaceType.asNullable()
        )
            .receiver(sectionInterfaceType)
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
        val DecoratableTx.utility: UtilityDecoratableFunctions
            get() = utilityOrNull ?: SubstrateApiException.moduleNotFound("Utility")
    */
    private fun sectionExtensionNameNonNull(
        moduleName: String,
        childInterfaceType: TypeName,
        sectionInterfaceType: TypeName,
    ): PropertySpec {
        val sectionExtensionNameNullable = sectionExtensionName(moduleName, nullable = true)
        val sectionExtensionNameNonNull = sectionExtensionName(moduleName, nullable = false)

        val getterBlock = """
            return $sectionExtensionNameNullable ?: %substrateExceptionClass:T.%moduleNotFound:M(%moduleName:S)
        """.trimIndent()

        val getterArgs = mapOf(
            "substrateExceptionClass" to substrateExceptionClass(),
            "moduleName" to moduleName,
            "moduleNotFound" to substrateExceptionMember("moduleNotFound"),
        )

        return PropertySpec.builder(
            name = sectionExtensionNameNonNull,
            type = childInterfaceType
        )
            .receiver(sectionInterfaceType)
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
    private fun elementInterface(moduleName: String, parentInterfaceTypeName: TypeName): TypeSpec {
        return TypeSpec.interfaceBuilder(interfaceName(moduleName))
            .addSuperinterface(parentInterfaceTypeName)
            .build()
    }

    private fun sectionExtensionName(moduleName: String, nullable: Boolean): String {
        val base = moduleName.decapitalize()

        return elementName(base, nullable)
    }

    protected fun substrateExceptionClass(): TypeName {
        return ClassName(DECORATABLE_API_PACKAGE, "SubstrateApiException")
    }

    protected fun substrateExceptionMember(name: String): MemberName {
        return MemberName(
            packageName = DECORATABLE_API_PACKAGE,
            simpleName = name,
            isExtension = true
        )
    }

    private fun interfaceType(moduleName: String, packageName: String) = ClassName(packageName, interfaceName(moduleName))

    private fun interfaceName(moduleName: String) = "${moduleName.capitalize()}$parentInterfaceName"
}
