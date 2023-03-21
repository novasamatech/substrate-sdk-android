package io.novasama.substrate_sdk_android.runtime.definitions.registry.extensions

import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.TypeProvider
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.GenericsExtension
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.stub.FakeType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GenericsExtensionTest {

    private val fakeTypeProvider: TypeProvider = {
        TypeReference(FakeType(it))
    }

    @Test
    fun `should extract raw type`() {
        val typeDef = "AccountInfo<T::Index, T::AccountData>"

        val createdType = GenericsExtension.createType("Test", typeDef, fakeTypeProvider)

        assert(createdType != null)
        assertEquals(createdType!!.name, "AccountInfo")
    }

    @Test
    fun `should return null for plain type`() {
        val typeDef = "AccountInfo"

        val createdType = GenericsExtension.createType("Test", typeDef, fakeTypeProvider)

        assert(createdType == null)
    }
}