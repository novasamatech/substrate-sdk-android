package io.novasama.substrate_sdk_android.runtime.definitions.registry

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Alias
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.BitVec
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Bytes
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.CallBytes
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Data
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.EraType
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.EventRecord
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericAccountId
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericConsensus
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericConsensusEngineId
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericEvent
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericMultiAddress
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericSeal
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericSealV0
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.H160
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.H256
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.H512
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Null
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.OpaqueCall
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.SessionKeysSubstrate
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.i128
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.i16
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.i256
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.i32
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.i64
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.i8
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u128
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u16
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u256
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u64
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.definitions.types.stub.FakeType

typealias TypePresetBuilder = MutableMap<String, TypeReference>
typealias TypePreset = Map<String, TypeReference>

fun TypePreset.newBuilder(): TypePresetBuilder = toMutableMap()

fun TypePresetBuilder.type(type: Type<*>) {
    val currentRef = getOrCreate(type.name)

    currentRef.value = type
}

fun TypePresetBuilder.fakeType(name: String) {
    type(FakeType(name))
}

fun TypePresetBuilder.alias(alias: String, original: String) {
    val aliasedReference = getOrCreate(original)

    val typeAlias = Alias(alias, aliasedReference)

    type(typeAlias)
}

fun TypePresetBuilder.getOrCreate(definition: String) = getOrPut(definition) { TypeReference(null) }

fun TypePresetBuilder.create(definition: String): TypeReference =
    TypeReference(null).also { put(definition, it) }

fun createTypePresetBuilder(): TypePresetBuilder = mutableMapOf()

fun typePreset(builder: TypePresetBuilder.() -> Unit): TypePreset {
    return createTypePresetBuilder().apply(builder)
}

fun v14Preset() = typePreset {
    type(BooleanType)

    type(u8)
    type(u16)
    type(u32)
    type(u64)
    type(u128)
    type(u256)

    type(i8)
    type(i16)
    type(i32)
    type(i64)
    type(i128)
    type(i256)

    type(Bytes)
    type(Null)
    type(H256)

    type(GenericCall)
    type(GenericEvent)
    type(EraType)

    type(Data(this))
    type(GenericAccountId)

    alias("Balance", "u128")
}

fun v13Preset(): TypePreset = typePreset {
    type(BooleanType)

    type(u8)
    type(u16)
    type(u32)
    type(u64)
    type(u128)
    type(u256)

    type(i8)
    type(i16)
    type(i32)
    type(i64)
    type(i128)
    type(i256)

    type(GenericAccountId)
    type(Null)
    type(GenericCall)

    fakeType("GenericBlock")

    type(H160)
    type(H256)
    type(H512)

    alias("GenericVote", "u8")

    type(Bytes)
    type(BitVec)

    type(Extrinsic)

    type(CallBytes) // seems to be unused in runtime
    type(EraType)
    type(Data(this))

    alias("BoxProposal", "Proposal")

    type(GenericConsensusEngineId)

    type(SessionKeysSubstrate(this))

    alias("GenericAccountIndex", "u32")

    type(GenericMultiAddress(this))

    type(OpaqueCall)

    type(GenericEvent)
    type(EventRecord(this))

    alias("<T::Lookup as StaticLookup>::Source", "LookupSource")
    alias("U64", "u64")
    alias("U32", "u32")

    alias("Bidkind", "BidKind")

    alias("AccountIdAddress", "GenericAccountId")

    alias("VoteWeight", "u128")
    alias("PreRuntime", "GenericPreRuntime")
    // todo replace with real type
    fakeType("GenericPreRuntime")
    type(GenericSealV0(this))
    type(GenericSeal(this))
    type(GenericConsensus(this))
}

fun TypePreset.unknownTypes() = entries
    .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }
