package github.nova_wallet.substrate_sdk_android.codegen

import com.google.gson.Gson
import io.github.nova_wallet.substrate_sdk_android.codegen.RuntimeMetadataRetriever
import io.github.nova_wallet.substrate_sdk_android.codegen.types.TypeRegistryCodegen
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import kotlinx.coroutines.runBlocking
import org.gradle.internal.impldep.org.testng.junit.JUnitTestRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CodegenTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `test`() {
        runBlocking {
            val gson = Gson()
            val gsonCodec = GsonCodec(gson)
            val runtimeMetadataRetriever = RuntimeMetadataRetriever(gsonCodec, null,"wss://rpc.polkadot.io")
            val folder = temporaryFolder.newFolder()

            runBlocking {
                val runtime = runtimeMetadataRetriever.constructRuntime()
                val typeRegistryCodegen = TypeRegistryCodegen(folder)
                typeRegistryCodegen.generateTypes(runtime.typeRegistry)
            }
        }
    }
}
