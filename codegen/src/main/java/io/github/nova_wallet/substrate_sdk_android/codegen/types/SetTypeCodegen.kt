package io.github.nova_wallet.substrate_sdk_android.codegen.types

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.asTypeName
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.SetType
import java.io.File

class SetTypeCodegen(
    parentDirectory: File,
    configuration: Configuration,
    typeUnfolding: TypeUnfolding,
) : TypeCodegen<SetType>(parentDirectory, configuration, typeUnfolding) {

    override fun FileSpec.Builder.applyType(type: SetType, path: TypePath) {
        // Set<String>
        val aliasedName = Set::class.asTypeName().parameterizedBy(String::class.asTypeName())

        addTypeAlias(
            TypeAliasSpec.builder(path.typeName, aliasedName)
                .build()
        )
    }
}
