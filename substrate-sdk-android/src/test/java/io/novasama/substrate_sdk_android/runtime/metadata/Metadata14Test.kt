package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.common.assertInstance
import io.novasama.substrate_sdk_android.common.assertNotInstance
import io.novasama.substrate_sdk_android.common.median
import io.novasama.substrate_sdk_android.getFileContentFromResources
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeResolver
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.unknownTypes
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v14Preset
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Alias
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Option
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Tuple
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Vec
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Null
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.DynamicByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.FixedByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u64
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u128
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Data
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.i64
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliases
import io.novasama.substrate_sdk_android.runtime.definitions.v14.TypesParserV14
import io.novasama.substrate_sdk_android.runtime.metadata.builder.VersionedRuntimeBuilder
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntryType
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RuntimeMetadataSchemaV14
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.metadata.MetadataTestCommon.buildPost14TestRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@RunWith(MockitoJUnitRunner::class)
class Metadata14Test {

    @Test
    fun `should decode metadata types v14`() {
        val inHex = getFileContentFromResources("westend_metadata_v14")
        val metadataReader = RuntimeMetadataReader.read(inHex)

        val typesBuilder = TypesParserV14.parse(
            metadataReader.metadata[RuntimeMetadataSchemaV14.lookup],
            v14Preset()
        )

        assertEquals(0, typesBuilder.unknownTypes().size)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `performance test`() {
        val times = 100

        val inHex = getFileContentFromResources("westend_metadata_v14")
        val metadataReader = RuntimeMetadataReader.read(inHex)

        val parseTimes = (0..times).map {
            measureTime {
                TypesParserV14.parse(
                    metadataReader.metadata[RuntimeMetadataSchemaV14.lookup],
                    v14Preset()
                )
            }.inMilliseconds
        }
        println("Median: ${parseTimes.median()} ms")
    }

    @Test
    fun `should decode westend metadata v14`() {
        val runtime = buildPost14TestRuntime("westend_metadata_v14")
        val metadata = runtime.metadata
        val typeRegistry = runtime.typeRegistry

        val accountReturnEntry = metadata.module("System").storage("Account").type
        assertInstance<StorageEntryType.NMap>(accountReturnEntry)

        val accountInfo = accountReturnEntry.value
        assertInstance<Struct>(accountInfo)
        val accountData = accountInfo.get<Struct>("data")
        requireNotNull(accountData)

        val systemRemarkType =
            metadata.module("System").call("remark").arguments.first().type?.skipAliases()
        assertInstance<DynamicByteArray>(systemRemarkType)

        val setPayeeVariant =
            metadata.module("Staking").call("set_payee").arguments.first().type?.skipAliases()
        assertInstance<DictEnum>(setPayeeVariant)

        // empty variant element -> null optimization
        assertInstance<Null>(setPayeeVariant["Staked"])
        // 1 field variant element -> unwrap struct optimization
        assertInstance<FixedByteArray>(setPayeeVariant["Account"])

        // multiple null-named elements in struct does not collapse into single one
        val dustLostEventArguments = metadata.module("Balances").event("DustLost").arguments
        assertEquals(2, dustLostEventArguments.size)

        // multiple null-named elements with same type in struct does not collapse into single one
        val transferEventArguments = metadata.module("Balances").event("Transfer").arguments
        assertEquals(3, transferEventArguments.size)

        assertEquals(4 to 2, metadata.module("Balances").event("Transfer").index)
        assertEquals(4 to 3, metadata.module("Balances").call("transfer_keep_alive").index)

        // id-based types should alias to path-based types
        val batchArgument = metadata.module("Utility").call("batch").arguments.first().type
        assertInstance<Vec>(batchArgument)
        val callType = batchArgument.innerType
        assertInstance<Alias>(callType)
        assertEquals("westend_runtime.RuntimeCall", callType.aliasedReference.value?.name)

        // Options should not be path-based
        val optionType = typeRegistry["Option"]
        assertNull(optionType)
        val activeEraReturnType = metadata.module("Staking").storage("ActiveEra").type.value
        assertInstance<Struct>(activeEraReturnType)
        val eraStartType = activeEraReturnType.get<Option>("start")?.innerType?.skipAliases()
        assertEquals(u64, eraStartType)

        // BTreeMaps should not be path-based
        val bTreemapType = typeRegistry["BTreeMap"]
        assertNull(bTreemapType)

        // Verify id-based BTreeMaps on failing case for path-based
        val eraRewardPointsReturnType =
            metadata.module("Staking").storage("ErasRewardPoints").type.value
        assertInstance<Struct>(eraRewardPointsReturnType)
        val individualType =
            eraRewardPointsReturnType.get<Vec>("individual")?.innerType?.skipAliases()
        assertInstance<Tuple>(individualType)
        val shouldBeAccountId = individualType[0]
        assertInstance<FixedByteArray>(shouldBeAccountId)
        assertEquals(32, shouldBeAccountId.length)
        assertEquals(u32, individualType[1])

        // verify FixedByteArray optimization works with lookahead types
        val accountId = typeRegistry["sp_core.crypto.AccountId32"]
        assertInstance<FixedByteArray>(accountId)

        // id-based types with empty path should not be aliased
        val u8Primitive = typeRegistry["2"]
        assertNotInstance<Alias>(u8Primitive)

        // Call type should alias to GenericCall
        assertInstance<GenericCall>(callType.skipAliases())

        // Identity data type should be aliased to Data
        val identityType = typeRegistry["pallet_identity.types.Data"]
        assertInstance<Data>(identityType?.skipAliases())

        // RuntimeDispatchInfo should be crated based on DispatchInfo
        val runtimeDispatchInfo = typeRegistry["RuntimeDispatchInfo"]
        assertNotNull(runtimeDispatchInfo)

        // Exrinsic types should be crated based on UncheckedExtrinsic
        val address = typeRegistry["Address"]
        assertEquals("sp_runtime.multiaddress.MultiAddress", address?.skipAliases()?.name)
        val signature = typeRegistry["ExtrinsicSignature"]
        assertEquals("sp_runtime.MultiSignature", signature?.skipAliases()?.name)

        // Balance type should be present
        val balanceType = typeRegistry["Balance"]
        assertEquals(u128, balanceType?.skipAliases())
    }

    @Test
    fun `should decode gov2 testnet runtime v14`() {
        val runtime = buildPost14TestRuntime("gov2_testnet_runtime_v14")
        val typeRegistry = runtime.typeRegistry

        // should parse signed primitives to IntType
        val fixedI64 = typeRegistry["sp_arithmetic.fixed_point.FixedI64"]?.skipAliases()
        assertEquals(i64, fixedI64)
    }
}