package io.novasama.substrate_sdk_android.runtime.definitions.registry.extensions

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import io.novasama.substrate_sdk_android.common.assertInstance
import io.novasama.substrate_sdk_android.getResourceReader
import io.novasama.substrate_sdk_android.runtime.definitions.TypeDefinitionParser
import io.novasama.substrate_sdk_android.runtime.definitions.TypeDefinitionsTree
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeResolver
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.TypeProvider
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.HashMapExtension
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v13Preset
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Tuple
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Vec
import io.novasama.substrate_sdk_android.runtime.definitions.types.stub.FakeType
import org.junit.Assert
import org.junit.Test

class HashMapExtensionTest {

    private val fakeTypeProvider: TypeProvider = {
        TypeReference(FakeType(it))
    }

    @Test
    fun `should parse hashmap`() {
        val type = HashMapExtension.createType("HashMap<Text, Text>", "HashMap<Text, Text>", fakeTypeProvider)
        assertInstance<Vec>(type)
        Assert.assertEquals(type.typeReference.value?.name, "(Text,Text)" )
    }

    @Test
    fun `should parse hashmap with tuple`() {
        val gson = Gson()
        val defaultReader = JsonReader(getResourceReader("default.json"))
        val defaultTree =
            gson.fromJson<TypeDefinitionsTree>(defaultReader, TypeDefinitionsTree::class.java)
        val defaultParsed = TypeDefinitionParser.parseBaseDefinitions(defaultTree, v13Preset())
        val defaultRegistry = TypeRegistry(defaultParsed, DynamicTypeResolver.defaultCompoundResolver())
        val type = defaultRegistry["HashMap<Text, Text>"]
        assertInstance<Vec>(type)
        assertInstance<Tuple>(type.typeReference.value)
    }
}
