package jp.co.soramitsu.fearless_utils.runtime

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.SignatureInstanceConstructor
import jp.co.soramitsu.fearless_utils.signing.MultiSignature
import jp.co.soramitsu.fearless_utils.test_shared.getFileContentFromResources

object RealRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val metadataRaw = readRawMetadata(networkName)
        val jsons = listOf(readDefaultJson(), readNetworkJson(networkName))

        return RuntimeFactory(GsonCodec(Gson())).create(metadataRaw, jsons)
    }

    fun buildRuntimeV14(networkName: String): RuntimeSnapshot {
        val metadataRaw = getFileContentFromResources("${networkName}_metadata_v14")
        val jsons = listOf(readNetworkJson(networkName))

        return RuntimeFactory(GsonCodec(Gson())).create(metadataRaw, jsons)
    }

    fun readRawMetadata(networkName: String = "kusama") =
        getFileContentFromResources("${networkName}_metadata")

    private fun readDefaultJson() = getFileContentFromResources("default.json")
    private fun readNetworkJson(networkName: String) = getFileContentFromResources("${networkName}.json")
}

object TestSignatureConstructor : SignatureInstanceConstructor {

    override fun constructInstance(typeRegistry: TypeRegistry, value: MultiSignature): Any? {
        return DictEnum.Entry(value.encryptionType, value.signature)
    }
}

