package io.novasama.substrate_sdk_android.runtime.definitions.types.instances

import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrThrow
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.MULTI_ADDRESS_ID
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.FixedByteArray

private const val ADDRESS_TYPE = "Address"

object AddressInstanceConstructor : RuntimeType.InstanceConstructor<AccountId> {

    override fun constructInstance(typeRegistry: TypeRegistry, value: AccountId): Any {
        return when (val addressType = typeRegistry.getOrThrow(ADDRESS_TYPE)) {
            is DictEnum -> { // MultiAddress
                DictEnum.Entry(MULTI_ADDRESS_ID, value)
            }
            is FixedByteArray -> { // GenericAccountId or similar
                value
            }
            else -> throw UnsupportedOperationException("Unknown address type: ${addressType.name}")
        }
    }
}
