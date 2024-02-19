package io.novasama.substrate_sdk_android.runtime.definitions

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import io.novasama.substrate_sdk_android.common.assertInstance
import io.novasama.substrate_sdk_android.getResourceReader
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeResolver
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePreset
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.type
import io.novasama.substrate_sdk_android.runtime.definitions.registry.typePreset
import io.novasama.substrate_sdk_android.runtime.definitions.registry.unknownTypes
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v13Preset
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.CollectionEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.FixedArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Option
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.SetType
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Tuple
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Vec
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.FixedByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u64
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.definitions.types.stub.FakeType
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


@RunWith(JUnit4::class)
class TypeDefinitionParserTest {

    val gson = Gson()

    @Test
    fun `should parse typealias`() {
        val A = FakeType("A")

        val initialTypeRegistry = typePreset {
            type(A)
        }

        val definitions = definitions {
            """
            "B": "A"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val B = resultRegistry["B"]

        assertEquals(A, B)
    }

    @Test
    fun `should parse struct`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typePreset {
            type(A)
            type(B)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "struct",
                "type_mapping": [
                    [
                        "a",
                        "A"
                    ],
                    [
                        "b",
                        "B"
                    ]
                ]
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Struct>(C)

        assertEquals(C["a"], A)
        assertEquals(C["b"], B)
    }

    @Test
    fun `should parse dict enum`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typePreset {
            type(A)
            type(B)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "enum",
                "type_mapping": [
                    [
                        "a",
                        "A"
                    ],
                    [
                        "b",
                        "B"
                    ]
                ]
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<DictEnum>(C)

        assertEquals(C["a"], A)
        assertEquals(C["b"], B)
    }

    @Test
    fun `should parse collection enum`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typePreset {
            type(A)
            type(B)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "enum",
                "value_list": [
                    "A",
                    "B"
                ]
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<CollectionEnum>(C)

        assertEquals(C[0], "A")
        assertEquals(C[1], "B")
    }

    @Test
    fun `should parse set`() {

        val initialTypeRegistry = typePreset {
            type(u8)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "set",
                "value_type": "u8",
                "value_list": {
                    "A": 1,
                    "B": 2
                }
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<SetType>(C)

        assertEquals(C["A"], 1.toBigInteger())
        assertEquals(C["B"], 2.toBigInteger())
    }

    @Test
    fun `should parse fixed array`() {

        val initialTypeRegistry = typePreset {
            type(BooleanType)
        }

        val definitions = definitions {
            """
            "C": "[bool; 5]"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<FixedArray>(C)

        assertEquals(C.length, 5)
        assertEquals(C.typeReference.value, BooleanType)
    }

    @Test
    fun `should parse fixed u8 array optimized`() {

        val initialTypeRegistry = typePreset {
            type(u8)
        }

        val definitions = definitions {
            """
            "C": "[u8; 5]"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<FixedByteArray>(C)

        assertEquals(C.length, 5)
    }

    @Test
    fun `should parse vector`() {

        val initialTypeRegistry = typePreset {
            type(BooleanType)
        }

        val definitions = definitions {
            """
            "C": "Vec<bool>"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Vec>(C)

        assertEquals(C.typeReference.value, BooleanType)
    }

    @Test
    fun `should parse tuple`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typePreset {
            type(A)
            type(B)
        }

        val definitions = definitions {
            """
            "C": "(A, B)"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Tuple>(C)

        assertEquals(C[0], A)
        assertEquals(C[1], B)
    }

    @Test
    fun `should parse option`() {
        val A = FakeType("A")

        val initialTypeRegistry = typePreset {
            type(A)
        }

        val definitions = definitions {
            """
            "C": "Option<A>"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Option>(C)

        assertEquals(C.innerType, A)
    }

    @Test
    fun `should parse complex type`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typePreset {
            type(A)
            type(B)
        }

        val definitions = definitions {
            """
            "C": "Vec<(A, Option<(A, B)>)>"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Vec>(C)

        val innerTuple = C.innerType
        assertInstance<Tuple>(innerTuple)

        assertEquals(A, innerTuple[0])

        val innerOption = innerTuple[1]
        assertInstance<Option>(innerOption)

        val leafTuple = innerOption.innerType
        assertInstance<Tuple>(leafTuple)

        assertEquals(A, leafTuple[0])
        assertEquals(B, leafTuple[1])
    }

    @Test
    fun `should put unresolved type to unknown`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typePreset {
            type(A)
            type(B)
        }

        val definitions = definitions {
            """
            "C": "F"
            """
        }

        val tree = gson.fromJson(definitions, TypeDefinitionsTree::class.java)

        val unknown = TypeDefinitionParser.parseBaseDefinitions(tree, initialTypeRegistry)
            .unknownTypes()

        assert("F" in unknown)
    }

    @Test
    fun `should parse substrate data`() {
        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))
        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)

        val result = TypeDefinitionParser.parseBaseDefinitions(tree, v13Preset())

        val unknownTypes = result.unknownTypes()

        print(unknownTypes)
        assertEquals(0, unknownTypes.size)
    }

    @Test
    fun `should parse network-specific patches`() {
        val gson = Gson()

        val defaultReader = JsonReader(getResourceReader("default.json"))
        val kusamaReader = JsonReader(getResourceReader("kusama.json"))

        val defaultTree =
            gson.fromJson<TypeDefinitionsTree>(defaultReader, TypeDefinitionsTree::class.java)
        val kusamaTree =
            gson.fromJson<TypeDefinitionsTree>(kusamaReader, TypeDefinitionsTree::class.java)

        val defaultParsed = TypeDefinitionParser.parseBaseDefinitions(defaultTree, v13Preset())
        val defaultRegistry =
            TypeRegistry(defaultParsed, DynamicTypeResolver.defaultCompoundResolver())

        val keysDefault = defaultRegistry["Keys"]
        assertEquals("SessionKeysSubstrate", keysDefault?.name)

        val kusamaParsed = TypeDefinitionParser.parseNetworkVersioning(
            kusamaTree,
            defaultParsed,
            1057
        )
        val kusamaRegistry =
            TypeRegistry(kusamaParsed, DynamicTypeResolver.defaultCompoundResolver())

        print(kusamaParsed.unknownTypes())

        val keysKusama = kusamaRegistry["Keys"]
        assertEquals("Keys", keysKusama?.name) // changed from SessionKeysSubstrate

        val assignments = kusamaRegistry["CompactAssignments"]
        assertEquals("CompactAssignmentsTo257", assignments?.name) // changed only at 2023

        val addressKusama = kusamaRegistry["Address"]
        assertEquals(
            "GenericAccountId",
            addressKusama?.name
        ) // Address changed to MultiAddress only in 2028

        val weight = kusamaRegistry["Weight"]
        assertEquals(u64, weight) // changed multiple times, latest at 1057

        val refCount = kusamaRegistry["RefCount"]
        assertEquals(u8, refCount)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    @Ignore
    fun `performance test`() {
        val times = 10

        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))

        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)

        val parseTimes = (0..times).map {
            measureTime {
                TypeDefinitionParser.parseBaseDefinitions(tree, v13Preset())
            }.inMilliseconds
        }

        println(parseTimes)
        print(parseTimes.average())
    }

    private fun parseFromJson(typePreset: TypePreset, json: String): TypeRegistry {
        val tree = gson.fromJson(json, TypeDefinitionsTree::class.java)

        return TypeRegistry(
            TypeDefinitionParser.parseBaseDefinitions(tree, typePreset),
            dynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver()
        )
    }

    private fun definitions(builder: () -> String): String {
        return """
            { 
            "types": {
                    ${builder.invoke()}
                }
             }
        """.trimIndent()
    }
}