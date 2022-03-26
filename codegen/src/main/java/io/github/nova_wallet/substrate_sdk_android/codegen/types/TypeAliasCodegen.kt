package io.github.nova_wallet.substrate_sdk_android.codegen.types

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeAliasSpec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import java.io.File

class TypeAliasCodegen(
    parentDirectory: File,
    configuration: Configuration,
) : TypeCodegen<Alias>(parentDirectory, configuration) {

    override fun FileSpec.Builder.applyType(type: Alias, path: TypePath) {
        val innerType = type.aliasedReference.value ?: unknownType(type.name, "alias")
        val innerTypeName = innerType.toTypeName(parentType = type.name)

        addTypeAlias(
            TypeAliasSpec.builder(path.typeName, innerTypeName)
                .build()
        )
    }
}
