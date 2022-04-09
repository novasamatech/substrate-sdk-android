package io.github.nova_wallet.substrate_sdk_android.codegen.common

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import io.github.nova_wallet.substrate_sdk_android.codegen.types.TypeCodegen
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.skipAliases
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

fun TypeName.asNullable() = copy(nullable = true)

fun TypeSpec.Builder.markSerializable() = addAnnotation(Serializable::class)

fun PropertySpec.Builder.maybeMarkAsContextual(configuration: TypeCodegen.Configuration, type: RuntimeType<*, *>): PropertySpec.Builder {
    type.skipAliases()?.let {
        if (it::class in configuration.needsContextual) {
            addAnnotation(Contextual::class)
        }
    }

    return this
}
