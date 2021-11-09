package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.fearless_utils.common.assertNotInstance
import jp.co.soramitsu.fearless_utils.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.v14Preset
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Null
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.DynamicByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.UIntType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.skipAliases
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.TypesParserV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.builder.VersionedRuntimeBuilder
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class Metadata14Test {

    @Test
    fun `should decode metadata types v14`() {
        val inHex = getFileContentFromResources("westend_metadata_v14")
        val metadataReader = RuntimeMetadataReader.read(inHex)
        val parseResult = TypesParserV14.parse(
            metadataReader.metadata[RuntimeMetadataSchemaV14.lookup],
            v14Preset()
        )
        assertEquals(0, parseResult.unknownTypes.size)
    }

    @Test
    fun `should decode metadata v14`() {
        val inHex = getFileContentFromResources("westend_metadata_v14")
        val metadataReader = RuntimeMetadataReader.read(inHex)
        val parseResult = TypesParserV14.parse(
            lookup = metadataReader.metadata[RuntimeMetadataSchemaV14.lookup],
            typePreset = v14Preset()
        )

        val typeRegistry = TypeRegistry(
            parseResult.typePreset,
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

        val systemRemarkType = metadata.module("System").call("remark").arguments.first().type?.skipAliases()
        assertInstance<DynamicByteArray>(systemRemarkType)

        val setPayeeVariant = metadata.module("Staking").call("set_payee").arguments.first().type?.skipAliases()
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

        // id-based types with empty path shold not be aliased
        val u8Primitive = typeRegistry["2"]
        assertNotInstance<Alias>(u8Primitive)
    }
}