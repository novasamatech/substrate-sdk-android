package io.novasama.substrate_sdk_android.encrypt.json

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.ContentCoderFactory
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.encode
import io.novasama.substrate_sdk_android.encrypt.json.coders.type.TypeCoderFactory
import io.novasama.substrate_sdk_android.encrypt.json.coders.type.encode
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.model.JsonAccountData
import org.bouncycastle.util.encoders.Base64

class JsonEncoder(
    private val gson: Gson
) {

    fun generate(
        keypair: Keypair,
        password: String,
        name: String,
        multiChainEncryption: MultiChainEncryption,
        genesisHash: String,
        address: String
    ): String {
        val encoding = when (multiChainEncryption) {
            is MultiChainEncryption.Substrate -> {
                JsonAccountData.Encoding.substrate(multiChainEncryption.encryptionType)
            }
            MultiChainEncryption.Ethereum -> {
                JsonAccountData.Encoding.ethereum()
            }
        }
        val encoded = formEncodedField(keypair, password, encoding)

        val importData = JsonAccountData(
            encoded = encoded,
            address = address,
            encoding = encoding,
            meta = JsonAccountData.Meta(
                name = name,
                whenCreated = System.currentTimeMillis(),
                genesisHash = genesisHash
            )
        )

        return gson.toJson(importData)
    }

    private fun formEncodedField(
        keypair: Keypair,
        password: String,
        encoding: JsonAccountData.Encoding
    ): String {

        val contentEncoder = ContentCoderFactory.getEncoder(encoding.content)!!
        val typeEncoder = TypeCoderFactory.getEncoder(encoding.type)!!

        val encodedContent = contentEncoder.encode(keypair)
        val encryptedContent = typeEncoder.encode(encodedContent, password.encodeToByteArray())

        return Base64.toBase64String(encryptedContent)
    }
}
