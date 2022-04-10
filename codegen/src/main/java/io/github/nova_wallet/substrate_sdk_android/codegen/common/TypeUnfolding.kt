package io.github.nova_wallet.substrate_sdk_android.codegen.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeFormatting.joinTypeName
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeFormatting.unknownType
import io.github.nova_wallet.substrate_sdk_android.codegen.types.*
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.types.Tuple2
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.*
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.DynamicByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import java.math.BigInteger

private const val MAX_TUPLE_DIMENSIONALITY = 8

class TypeUnfolding(
    private val configuration: TypeCodegen.Configuration
) {

    fun runtimeTypeToTypeName(runtimeType: RuntimeType<*, *>, parent: String): TypeName = with(runtimeType) {
        if (name in configuration.predefinedTypes) return configuration.predefinedTypes.getValue(name)

        val typePath = TypePath.fromName(name)

        return unfold(this, typePath, parent)
    }

    private fun unfold(type: RuntimeType<*, *>, typePath: TypePath, parentType: String): TypeName {
        return when (type) {
            is Vec -> parametrizedList(parentType, type.name, type.typeReference)

            is FixedArray -> parametrizedList(parentType, type.name, type.typeReference)

            is DynamicByteArray, is FixedByteArray -> ByteArray::class.asTypeName()

            is Tuple -> parametrizedTuple(type, parentType)

            is Alias -> if (typePath.isIdLike || isPathLikeAliasUnfoldable(type)) {
                type.aliasedReference.value?.let { runtimeTypeToTypeName(it, parentType) }
                    ?: unknownType(type.name, "alias")
            } else {
                typePath.toTypeName()
            }

            is Option -> type.innerType?.let { runtimeTypeToTypeName(it, parentType).asNullable() }
                ?: unknownType(type.name, "optional")

            is Compact -> BigInteger::class.asTypeName()

            else -> typePath.toTypeName()
        }
    }

    // For better usability collections are unfolded to provide a clearer via (for example against BoundedVecXXX)
    private fun isPathLikeAliasUnfoldable(alias: Alias) = when (alias.aliasedReference.value) {
        is Vec, is FixedArray -> true
        else -> false
    }

    private fun parametrizedTuple(tuple: Tuple, parentType: String): TypeName {
        val directionality = tuple.typeReferences.size

        require(directionality <= MAX_TUPLE_DIMENSIONALITY) {
            "Cannot decode tuple of length $directionality"
        }

        val packageName = Tuple2::class.asClassName().packageName
        val typeParameters = tuple.typeReferences.mapIndexed { idx, reference ->
            val type = reference.value ?: unknownType(tuple.name, child = idx.toString())

            runtimeTypeToTypeName(type, parentType joinTypeName tuple.name)
        }
        val tupleName = ClassName(packageName, "Tuple$directionality")

        return if (typeParameters.isNotEmpty()) {
            tupleName.parameterizedBy(typeParameters)
        } else {
            tupleName
        }
    }

    private fun parametrizedList(parentName: String, typeName: String, with: TypeReference): TypeName {
        val innerType = with.value ?: unknownType(typeName, "List inner type")
        val innerTypeName = runtimeTypeToTypeName(innerType, parentName)

        return List::class.asClassName().parameterizedBy(innerTypeName)
    }
}
