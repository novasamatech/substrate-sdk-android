package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import java.math.BigInteger

interface WithName {
    val name: String
}

fun <T : WithName> List<T>.groupByName() = associateBy(WithName::name).toMap()

class RuntimeMetadata(
    val runtimeVersion: BigInteger,
    val modules: Map<String, Module>,
    val extrinsic: ExtrinsicMetadata
) {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<RuntimeMetadataSchema>
    ) : this(
        runtimeVersion = struct[RuntimeMetadataSchema.runtimeVersion].toInt().toBigInteger(),
        modules = struct[RuntimeMetadataSchema.modules].map { Module(typeRegistry, it) }
            .groupByName(),
        extrinsic = ExtrinsicMetadata(struct[RuntimeMetadataSchema.extrinsic])
    )
}

private fun EncodableStruct<ModuleMetadataSchema>.indexForChild(childIndex: Int): Pair<Int, Int> {
    return this[ModuleMetadataSchema.index].toInt() to childIndex
}

class Module(
    override val name: String,
    val storage: Storage?,
    val calls: Map<String, Function>?,
    val events: Map<String, Event>?,
    val constants: Map<String, Constant>,
    val errors: Map<String, Error>,
    val index: BigInteger
) : WithName {

    constructor(
        typeRegistry: TypeRegistry,
        moduleStruct: EncodableStruct<ModuleMetadataSchema>
    ) : this(
        name = moduleStruct.name,
        storage = moduleStruct[ModuleMetadataSchema.storage]?.let {
            Storage(
                typeRegistry,
                it,
                moduleStruct.name
            )
        },
        calls = moduleStruct[ModuleMetadataSchema.calls]?.mapIndexed { functionIndex, functionStruct ->
            Function(typeRegistry, functionStruct, moduleStruct.indexForChild(functionIndex))
        }?.groupByName(),

        events = moduleStruct[ModuleMetadataSchema.events]?.mapIndexed { eventIndex, eventStruct ->
            Event(typeRegistry, eventStruct, index = moduleStruct.indexForChild(eventIndex))
        }?.groupByName(),

        constants = moduleStruct[ModuleMetadataSchema.constants].map { Constant(typeRegistry, it) }
            .groupByName(),

        errors = moduleStruct[ModuleMetadataSchema.errors].map(::Error).groupByName(),
        index = moduleStruct[ModuleMetadataSchema.index].toInt().toBigInteger()
    )
}

private val EncodableStruct<ModuleMetadataSchema>.name: String
    get() = get(ModuleMetadataSchema.name)

class Storage(
    val prefix: String,
    val entries: Map<String, StorageEntry>
) {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<StorageMetadataSchema>,
        moduleName: String
    ) : this(
        prefix = struct[StorageMetadataSchema.prefix],
        entries = struct[StorageMetadataSchema.entries].map {
            StorageEntry(
                typeRegistry,
                it,
                moduleName
            )
        }
            .groupByName()
    )

    operator fun get(entry: String) = entries[entry]
}

class StorageEntry(
    override val name: String,
    val modifier: StorageEntryModifier,
    val type: StorageEntryType,
    val default: ByteArray,
    val documentation: List<String>,
    val moduleName: String
) : WithName {

    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<StorageEntryMetadataSchema>,
        moduleName: String
    ) : this(
        name = struct[StorageEntryMetadataSchema.name],
        modifier = struct[StorageEntryMetadataSchema.modifier],
        type = StorageEntryType.from(typeRegistry, struct[StorageEntryMetadataSchema.type]),
        default = struct[StorageEntryMetadataSchema.default],
        documentation = struct[StorageEntryMetadataSchema.documentation],
        moduleName = moduleName
    )
}

sealed class StorageEntryType(
    val value: Type<*>?
) {

    companion object {
        fun from(typeRegistry: TypeRegistry, value: Any?) = when (value) {
            is String -> Plain(typeRegistry, value)
            is EncodableStruct<*> -> when (value.schema) {
                MapSchema -> Map(typeRegistry, value)
                DoubleMapSchema -> DoubleMap(typeRegistry, value)
                NMapSchema -> NMap(typeRegistry, value)
                else -> cannotConstruct(value)
            }
            else -> cannotConstruct(value)
        }

        private fun cannotConstruct(from: Any?): Nothing {
            throw IllegalArgumentException("Cannot construct StorageEntryType from $from")
        }
    }

    class Plain(value: Type<*>?) : StorageEntryType(value) {

        constructor(
            typeRegistry: TypeRegistry,
            typeDef: String
        ) : this(
            typeRegistry[typeDef]
        )
    }

    class Map(
        val hasher: StorageHasher,
        val key: Type<*>?,
        value: Type<*>?,
        val unused: Boolean
    ) : StorageEntryType(value) {

        constructor(
            typeRegistry: TypeRegistry,
            struct: EncodableStruct<*>
        ) : this(
            hasher = struct[MapSchema.hasher],
            key = typeRegistry[struct[MapSchema.key]],
            value = typeRegistry[struct[MapSchema.value]],
            unused = struct[MapSchema.unused]
        )
    }

    class DoubleMap(
        val key1Hasher: StorageHasher,
        val key1: Type<*>?,
        val key2: Type<*>?,
        value: Type<*>?,
        val key2Hasher: StorageHasher
    ) : StorageEntryType(value) {
        constructor(
            typeRegistry: TypeRegistry,
            struct: EncodableStruct<*>
        ) : this(
            key1Hasher = struct[DoubleMapSchema.key1Hasher],
            key2Hasher = struct[DoubleMapSchema.key2Hasher],
            key1 = typeRegistry[struct[DoubleMapSchema.key1]],
            key2 = typeRegistry[struct[DoubleMapSchema.key2]],
            value = typeRegistry[struct[DoubleMapSchema.value]]
        )
    }

    class NMap(
        val keys: List<Type<*>?>,
        val hashers: List<StorageHasher>,
        value: Type<*>?
    ) : StorageEntryType(value) {
        constructor(
            typeRegistry: TypeRegistry,
            struct: EncodableStruct<*>
        ) : this(
            keys = struct[NMapSchema.keys].map { typeRegistry[it] },
            hashers = struct[NMapSchema.hashers],
            value = typeRegistry[struct[NMapSchema.value]]
        )
    }
}

class Function(
    override val name: String,
    val arguments: List<FunctionArgument>,
    val documentation: List<String>,
    val index: Pair<Int, Int>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<FunctionMetadataSchema>,
        index: Pair<Int, Int>
    ) : this(
        name = struct[FunctionMetadataSchema.name],
        arguments = struct[FunctionMetadataSchema.arguments].map {
            FunctionArgument(typeRegistry, it)
        },
        documentation = struct[FunctionMetadataSchema.documentation],
        index = index
    )
}

class FunctionArgument(
    override val name: String,
    val type: Type<*>?
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<FunctionArgumentMetadataSchema>
    ) : this(
        name = struct[FunctionArgumentMetadataSchema.name],
        type = typeRegistry[struct[FunctionArgumentMetadataSchema.type]]
    )
}

class Event(
    override val name: String,
    val index: Pair<Int, Int>,
    val arguments: List<MetadataType>,
    val documentation: List<String>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<EventMetadataSchema>,
        index: Pair<Int, Int>
    ) : this(
        name = struct[EventMetadataSchema.name],
        arguments = struct[EventMetadataSchema.arguments].map { MetadataType(it, typeRegistry) },
        documentation = struct[EventMetadataSchema.documentation],
        index = index
    )
}

class Constant(
    override val name: String,
    val type: Type<*>?,
    val value: ByteArray,
    val documentation: List<String>
) : WithName {
    constructor(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<ModuleConstantMetadataSchema>
    ) : this(
        name = struct[ModuleConstantMetadataSchema.name],
        type = typeRegistry[struct[ModuleConstantMetadataSchema.type]],
        value = struct[ModuleConstantMetadataSchema.value],
        documentation = struct[ModuleConstantMetadataSchema.documentation]
    )
}

class Error(
    override val name: String,
    val documentation: List<String>
) : WithName {
    constructor(
        struct: EncodableStruct<ErrorMetadataSchema>
    ) : this(
        name = struct[ErrorMetadataSchema.name],
        documentation = struct[ErrorMetadataSchema.documentation]
    )
}

class ExtrinsicMetadata(
    val version: BigInteger,
    val signedExtensions: List<String>
) {
    constructor(
        struct: EncodableStruct<ExtrinsicMetadataSchema>
    ) : this(
        version = struct[ExtrinsicMetadataSchema.version].toInt().toBigInteger(),
        signedExtensions = struct[ExtrinsicMetadataSchema.signedExtensions]
    )
}

class MetadataType(
    val definition: String,
    val derivedType: Type<*>?
)

fun MetadataType(definition: String, typeRegistry: TypeRegistry) = MetadataType(
    definition = definition,
    derivedType = typeRegistry[definition]
)