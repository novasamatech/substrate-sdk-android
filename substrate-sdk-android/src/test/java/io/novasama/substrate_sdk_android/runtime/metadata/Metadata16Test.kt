package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.getFileContentFromResources
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.registry.unknownTypes
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v14Preset
import io.novasama.substrate_sdk_android.runtime.definitions.v14.TypesParserV14
import io.novasama.substrate_sdk_android.runtime.metadata.MetadataTestCommon.buildPost14TestRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigInteger

private const val METADATA_V16 = "paseo_people_metadata_v16"

@RunWith(MockitoJUnitRunner::class)
class Metadata16Test {

    private val runtime: RuntimeSnapshot = buildPost14TestRuntime(METADATA_V16)

    @Test
    fun `should decode metadata types v16`() {
        val inHex = getFileContentFromResources(METADATA_V16)
        val reader = RuntimeMetadataReader.read(inHex)

        assertEquals(16, reader.metadataVersion)

        val typesBuilder = TypesParserV14.parse(reader.lookup, v14Preset())

        assertEquals(0, typesBuilder.unknownTypes().size)
    }

    @Test
    fun `should decode runtime apis v16`() {
        val metadata = runtime.metadata

        // apis remain supported (and now carry version + deprecation info on the wire)
        assertNotNull(metadata.apis)
        assertNotNull(metadata.runtimeApiOrNull("Core"))

        // the runtime api used to dispatch view functions must be present in a v16 runtime
        assertNotNull(metadata.runtimeApiOrNull(VIEW_FUNCTION_RUNTIME_API_METHOD.substringBefore("_")))
    }

    @Test
    fun `should resolve view functions per module`() {
        val viewFunction = runtime.metadata.module("Resources").viewFunction("current_stmt_store_period")

        assertEquals("current_stmt_store_period", viewFunction.name)
        assertEquals("Resources", viewFunction.palletName)
        assertTrue(viewFunction.inputs.isEmpty())
        assertEquals("u32", viewFunction.output?.name)
        assertEquals(32, viewFunction.id.size)

        assertNull(runtime.metadata.module("Resources").viewFunctionOrNull("non_existent_view_function"))
    }

    @Test
    fun `should look up view function by id`() {
        val viewFunction = runtime.metadata.module("Resources").viewFunction("stmt_store_slot_context_for")

        val foundById = runtime.metadata.findViewFunction(viewFunction.id)

        assertNotNull(foundById)
        assertEquals(viewFunction.name, foundById!!.name)
        assertEquals(viewFunction.palletName, foundById.palletName)

        // unknown id yields null
        assertNull(runtime.metadata.findViewFunction(ByteArray(32)))
    }

    @Test
    fun `should build view function request without inputs`() {
        val viewFunction = runtime.metadata.module("Resources").viewFunction("current_stmt_store_period")

        val request = viewFunction.createRequest(runtime, emptyMap())
        val (rpcName, encodedArguments) = request.params

        assertEquals(VIEW_FUNCTION_RUNTIME_API_METHOD, rpcName)
        // id ([u8; 32], raw) followed by an empty Vec<u8> (compact length 0 -> 0x00)
        val expected = "0x" + viewFunction.id.toHexString() + "00"
        assertEquals(expected, encodedArguments)
    }

    @Test
    fun `should encode view function inputs`() {
        val viewFunction = runtime.metadata.module("Resources").viewFunction("stmt_store_slot_context_for")

        val inputs = mapOf(
            "period" to BigInteger.ONE,
            "seq" to BigInteger.valueOf(2)
        )

        val encodedInputs = viewFunction.encodeInputs(runtime, inputs).toHexString(withPrefix = true)
        // two little-endian u32: 1, 2
        assertEquals("0x0100000002000000", encodedInputs)

        val request = viewFunction.createRequest(runtime, inputs)
        val (_, encodedArguments) = request.params
        // id ++ Vec<u8>(8 bytes) -> compact length 8 == 0x20
        val expected = "0x" + viewFunction.id.toHexString() + "20" + "0100000002000000"
        assertEquals(expected, encodedArguments)
    }

    @Test
    fun `should decode view function output`() {
        val viewFunction = runtime.metadata.module("Resources").viewFunction("current_stmt_store_period")

        // Ok payload of the RuntimeViewFunction result: a u32 (little-endian 10)
        val decoded = viewFunction.decodeOutput(runtime, "0x0a000000")

        assertEquals(BigInteger.valueOf(10), decoded)
    }

    @Test
    fun `should still decode modules for v16`() {
        val metadata = runtime.metadata

        // sanity check that regular pallet items still parse alongside the new v16 sections
        assertFalse(metadata.modules.isEmpty())
        assertNotNull(metadata.module("System").storage("Account"))

        // v16 extrinsic exposes a set of supported format versions; we surface the newest supported one
        assertEquals(BigInteger.valueOf(5), metadata.extrinsic.version)
        assertTrue(metadata.extrinsic.signedExtensions.isNotEmpty())
    }
}
