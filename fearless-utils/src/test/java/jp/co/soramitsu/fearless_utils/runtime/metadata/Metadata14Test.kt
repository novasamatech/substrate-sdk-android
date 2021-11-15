package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.fearless_utils.common.assertNotInstance
import jp.co.soramitsu.fearless_utils.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.unknownTypes
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.v14Preset
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Option
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Null
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.DynamicByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.UIntType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u32
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u64
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.skipAliases
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.TypesParserV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.builder.VersionedRuntimeBuilder
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Ignore
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
    @Ignore
    fun `performance test`() {
        val times = 10

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

        println(parseTimes)
        print(parseTimes.average())
    }

    @Test
    fun `should decode metadata v14`() {
        val inHex = getFileContentFromResources("westend_metadata_v14")
        val metadataReader = RuntimeMetadataReader.read(inHex)
        val typePreset = TypesParserV14.parse(
            lookup = metadataReader.metadata[RuntimeMetadataSchemaV14.lookup],
            typePreset = v14Preset()
        )

        val typeRegistry = TypeRegistry(
            typePreset,
            DynamicTypeResolver.defaultCompoundResolver()
        )
        val metadata = VersionedRuntimeBuilder.buildMetadata(metadataReader, typeRegistry)

        val accountReturnEntry = metadata.module("System").storage("Account").type
        assertInstance<StorageEntryType.NMap>(accountReturnEntry)

        val accountInfo = accountReturnEntry.value
        assertInstance<Struct>(accountInfo)
        val accountData = accountInfo.get<Struct>("data")
        requireNotNull(accountData)
        val misFrozenType = accountData.get<UIntType>("miscFrozen")
        assertNotNull(misFrozenType) // test that snake case -> camel case is performed

        val systemRemarkType =
            metadata.module("System").call("remark").arguments.first().type?.skipAliases()
        assertInstance<DynamicByteArray>(systemRemarkType)

        val setPayeeVariant =
            metadata.module("Staking").call("set_payee").arguments.first().type?.skipAliases()
        assertInstance<DictEnum>(setPayeeVariant)

        // empty variant element -> null optimization
        assertInstance<Null>(setPayeeVariant["Staked"])
        // 1 field variant element -> unwrap struct optimization
        assertInstance<FixedArray>(setPayeeVariant["Account"])

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
        assertEquals("westend_runtime.Call", callType.aliasedReference.value?.name)

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
        assertInstance<FixedArray>(shouldBeAccountId)
        assertEquals(32, shouldBeAccountId.length)
        assertEquals(u8, shouldBeAccountId.innerType())
        assertEquals(u32, individualType[1])

        // id-based types with empty path shold not be aliased
        val u8Primitive = typeRegistry["2"]
        assertNotInstance<Alias>(u8Primitive)
    }
}