package jp.co.soramitsu.fearless_utils.runtime.metadata

import jp.co.soramitsu.fearless_utils.test_shared.assertInstance
import jp.co.soramitsu.fearless_utils.test_shared.assertNotInstance
import jp.co.soramitsu.fearless_utils.runtime.RealRuntimeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Option
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Null
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.DynamicByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.UIntType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u32
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u64
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.skipAliases
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class Metadata14Test {

    @Test
    fun `should decode metadata v14`() {

        val runtime = RealRuntimeProvider.buildRuntimeV14("westend")
        val metadata = runtime.metadata
        val typeRegistry = runtime.typeRegistry

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
        assertEquals("westend_runtime.Call", callType.aliasedReference.value?.name)

        // Options should not clash by name
        val optionTypes = typeRegistry.withPrefix("Option")
        assert(optionTypes.size > 1)

        val activeEraReturnType = metadata.module("Staking").storage("ActiveEra").type.value
        assertInstance<Struct>(activeEraReturnType)
        val eraStartType = activeEraReturnType.get<Option>("start")?.innerType?.skipAliases()
        assertEquals(u64, eraStartType)

        // BTreeMaps  should not clash by named
        val bTreemapTypes = typeRegistry.withPrefix("BTreeMap")
        assert(bTreemapTypes.size > 1)

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
    }

    private fun TypeRegistry.withPrefix(namePrefix: String) = types.filterKeys { it.startsWith(namePrefix) }.values
}
