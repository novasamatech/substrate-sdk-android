package io.novasama.substrate_sdk_android.runtime.definitions.registry.extensions

import io.novasama.substrate_sdk_android.common.assertInstance
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.VectorExtension
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Vec
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.DynamicByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import org.junit.Test

class VectorExtensionTest {

    @Test
    fun `should create optimized type for u8`() {
        val result = VectorExtension.createWrapper("A", TypeReference(u8))

        assertInstance<DynamicByteArray>(result)
    }

    @Test
    fun `should create vec type for other type`() {
        val result = VectorExtension.createWrapper("A", TypeReference(BooleanType))

        assertInstance<Vec>(result)
    }
}