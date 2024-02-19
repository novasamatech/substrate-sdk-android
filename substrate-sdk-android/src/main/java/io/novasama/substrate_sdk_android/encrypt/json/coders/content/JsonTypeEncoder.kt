package io.novasama.substrate_sdk_android.encrypt.json.coders.content

import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair

interface JsonContentEncoder {

    val secretEncoder: SecretEncoder

    val checksumEncoder: ChecksumEncoder

    interface ChecksumEncoder {

        fun encode(
            values: List<ByteArray>
        ): ByteArray
    }

    interface SecretEncoder {

        fun encode(
            keypair: Keypair,
            seed: ByteArray?
        ): List<ByteArray>
    }
}

fun JsonContentEncoder.encode(
    keypair: Keypair,
    seed: ByteArray?
) = checksumEncoder.encode(secretEncoder.encode(keypair, seed))

interface JsonContentDecoder {

    val checksumDecoder: ChecksumDecoder

    val secretDecoder: SecretDecoder

    interface ChecksumDecoder {

        fun decode(data: ByteArray): List<ByteArray>
    }

    interface SecretDecoder {

        class DecodedSecret(
            val seed: ByteArray?,
            val multiChainEncryption: MultiChainEncryption,
            val keypair: Keypair
        )

        /**
         * @return null if secret is not correct. Decrypted data otherwise
         */
        fun decode(data: List<ByteArray>): DecodedSecret
    }
}

fun JsonContentDecoder.decode(
    data: ByteArray
) = secretDecoder.decode(checksumDecoder.decode(data))

interface JsonChecksumCoder :
    JsonContentEncoder.ChecksumEncoder,
    JsonContentDecoder.ChecksumDecoder

interface JsonSecretCoder :
    JsonContentEncoder.SecretEncoder,
    JsonContentDecoder.SecretDecoder
