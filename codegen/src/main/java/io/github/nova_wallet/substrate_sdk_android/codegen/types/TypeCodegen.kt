package io.github.nova_wallet.substrate_sdk_android.codegen.types

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import kotlinx.serialization.UseSerializers
import java.io.File
import kotlin.reflect.KClass

abstract class TypeCodegen<T : RuntimeType<*, *>>(
    private val parentDirectory: File,
    protected val configuration: Configuration,
    protected val typeUnfolding: TypeUnfolding,
) {

    class Configuration(
        val predefinedTypes: Map<String, TypeName>,
        val needsContextual: Set<KClass<out RuntimeType<*, *>>>
    )

    fun RuntimeType<*, *>.toTypeName(parentType: String) = typeUnfolding.runtimeTypeToTypeName(this, parentType)

    abstract fun FileSpec.Builder.applyType(type: T, path: TypePath)

    fun generate(typeName: String, type: T) {
        val path = TypePath.fromName(typeName)

        if (path.isIdLike) {
            return
        }

        FileSpec.builder(path.packageName, path.typeName)
            .apply { applyType(type, path) }
            .addAnnotation(
                AnnotationSpec.builder(UseSerializers::class)
                    .addMember("%T::class", BigIntegerSerializer::class)
                    .build()
            )
            .build()
            .writeTo(parentDirectory)
    }
}
