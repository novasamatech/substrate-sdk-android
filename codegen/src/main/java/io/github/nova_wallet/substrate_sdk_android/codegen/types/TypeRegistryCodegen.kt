package io.github.nova_wallet.substrate_sdk_android.codegen.types

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.SetType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Data
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericEvent
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.*
import java.io.File
import java.math.BigInteger

private fun MutableMap<String, TypeName>.putType(type: RuntimeType<*, *>) {
    put(type.name, type::class.asTypeName())
}

@OptIn(ExperimentalStdlibApi::class)
class TypeRegistryCodegen(parentDirectory: File) {

    private val numbers = listOf(u8, u16, u32, u64, u128)
    private val predefinedTypes = buildMap {
        putType(BooleanType)

        numbers.forEach {
            put(it.name, BigInteger::class.asTypeName())
        }

        putType(GenericCall)
        putType(GenericEvent)
        put(Data.TYPE_NAME, Data::class.asTypeName())
    }

    private val configuration = TypeCodegen.Configuration(predefinedTypes)
    private val typesDirectory = File(parentDirectory, "types")

    private val structTypeCodegen = StructTypeCodegen(typesDirectory, configuration)
    private val aliasCodegen = TypeAliasCodegen(typesDirectory, configuration)
    private val variantCodegen = VariantCodegen(typesDirectory, configuration, structTypeCodegen)
    private val setCodegen = SetTypeCodegen(typesDirectory, configuration)

    fun generateTypes(typeRegistry: TypeRegistry) {
        typeRegistry.types.forEach { (typeName, typeReference) ->
            when (val type = typeReference.value) {
                is Struct -> structTypeCodegen.generate(typeName, type)
                is Alias -> aliasCodegen.generate(typeName, type)
                is DictEnum -> variantCodegen.generate(typeName, type)
                is SetType -> setCodegen.generate(typeName, type)
                null -> throw IllegalStateException("Corrupted typeRegistry - $typeName is null")
            }
        }
    }
}
