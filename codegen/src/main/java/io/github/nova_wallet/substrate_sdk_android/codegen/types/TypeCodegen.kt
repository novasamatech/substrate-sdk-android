package io.github.nova_wallet.substrate_sdk_android.codegen.types

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.nova_wallet.substrate_sdk_android.codegen.ext.asNullable
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.types.Tuple2
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.*
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.DynamicByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import java.io.File
import java.math.BigInteger

private const val MAX_TUPLE_DIMENSIONALITY = 8

abstract class TypeCodegen<T : RuntimeType<*, *>>(
    private val parentDirectory: File,
    private val configuration: Configuration
) {

    class Configuration(
        val predefinedTypes: Map<String, TypeName>
    )

    abstract fun FileSpec.Builder.applyType(type: T, path: TypePath)

    fun generate(typeName: String, type: T) {
        val path = TypePath.fromName(typeName)

        if (path.isIdLike) {
            return
        }

        FileSpec.builder(path.packageName, path.typeName)
            .apply { applyType(type, path) }
            .build()
            .writeTo(parentDirectory)
    }

    protected fun unknownType(typeName: String, fieldName: String): Nothing {
        throw IllegalArgumentException("Unknown type in $typeName::$fieldName")
    }

    protected fun RuntimeType<*, *>.toTypeName(parentType: String): TypeName {
        if (name in configuration.predefinedTypes) return configuration.predefinedTypes.getValue(name)

        val typePath = TypePath.fromName(name)

        return unfold(parentType, this, typePath)
    }

    protected infix fun String.joinTypeName(child: String) = "${this}.$child"

    private fun unfold(parentType: String, type: RuntimeType<*, *>, typePath: TypePath): TypeName {
        return when (type) {
            is Vec -> parametrizedList(parentType, type.name, type.typeReference)
            is FixedArray -> parametrizedList(parentType, type.name, type.typeReference)
            is DynamicByteArray, is FixedByteArray -> ByteArray::class.asTypeName()
            is Tuple -> parametrizedTuple(parentType, type)
            is Alias -> if (typePath.isIdLike || isPathLikeAliasUnfoldable(type)) {
                type.aliasedReference.value?.toTypeName(parentType) ?: unknownType(type.name, "alias")
            } else {
                typePath.toTypeName()
            }
            is Option -> type.innerType?.toTypeName(parentType)?.asNullable() ?: unknownType(type.name, "optional")
            is Compact -> BigInteger::class.asTypeName()
            else -> typePath.toTypeName()
        }
    }

    // For better usability collections are unfolded to provide a clearer via (for example against BoundedVecXXX)
    private fun isPathLikeAliasUnfoldable(alias: Alias) = when (alias.aliasedReference.value) {
        is Vec, is FixedArray -> true
        else -> false
    }

    private fun parametrizedTuple(parentType: String, tuple: Tuple): TypeName {
        val directionality = tuple.typeReferences.size

        require(directionality <= MAX_TUPLE_DIMENSIONALITY) {
            "Cannot decode tuple of length $directionality"
        }

        val packageName = Tuple2::class.asClassName().packageName
        val typeParameters = tuple.typeReferences.mapIndexed { idx, reference ->
            val type = reference.value ?: unknownType(tuple.name, fieldName = idx.toString())

            type.toTypeName(parentType joinTypeName tuple.name)
        }
        val tupleName = ClassName(packageName, "Tuple${directionality}")

        return if (typeParameters.isNotEmpty()) {
            tupleName.parameterizedBy(typeParameters)
        } else {
            tupleName
        }
    }

    private fun parametrizedList(parentName: String, typeName: String, with: TypeReference): TypeName {
        val innerType = with.value ?: unknownType(typeName, "List inner type")

        return List::class.asClassName().parameterizedBy(innerType.toTypeName(parentName))
    }
}
