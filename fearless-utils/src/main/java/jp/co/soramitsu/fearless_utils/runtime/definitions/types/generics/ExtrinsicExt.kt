package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.SignedExtension

class MultiSignature(val encryptionType: jp.co.soramitsu.fearless_utils.keyring.EncryptionType, val value: ByteArray)

fun Extrinsic.Signature.tryExtractMultiSignature(): MultiSignature? {
    val enumEntry = signature as? DictEnum.Entry<*> ?: return null
    val value = enumEntry.value as? ByteArray ?: return null

    val encryptionType =
        jp.co.soramitsu.fearless_utils.keyring.EncryptionType.fromStringOrNull(enumEntry.name.toLowerCase()) ?: return null

    return MultiSignature(encryptionType, value)
}

fun Extrinsic.Companion.create(customSignedExtensions: Collection<SignedExtension>): Extrinsic {
    val customSignedExtensionTypes = customSignedExtensions.associateBy(
        keySelector = { it.name },
        valueTransform = { it.type }
    )

    val allSignedExtensions = SignedExtras.default.extras + customSignedExtensionTypes

    return Extrinsic(ExtrinsicPayloadExtras(allSignedExtensions))
}

private val jp.co.soramitsu.fearless_utils.keyring.EncryptionType.multiSignatureName
    get() = rawName.capitalize()

fun MultiSignature.prepareForEncoding(): Any {
    return DictEnum.Entry(encryptionType.multiSignatureName, value)
}

fun <A> Extrinsic.Signature.Companion.new(
    accountIdentifier: A,
    signature: Any?,
    signedExtras: ExtrinsicPayloadExtrasInstance
) = Extrinsic.Signature(
    accountIdentifier = accountIdentifier,
    signature = signature,
    signedExtras = signedExtras
)

fun multiAddressFromId(addressId: ByteArray): DictEnum.Entry<ByteArray> {
    return DictEnum.Entry(
        name = MULTI_ADDRESS_ID,
        value = addressId
    )
}

fun Extrinsic.EncodingInstance(
    signature: Extrinsic.Signature?,
    call: GenericCall.Instance
): Extrinsic.EncodingInstance {
    return Extrinsic.EncodingInstance(
        signature,
        Extrinsic.EncodingInstance.CallRepresentation.Instance(call)
    )
}

fun Extrinsic.EncodingInstance(
    signature: Extrinsic.Signature?,
    callBytes: ByteArray
): Extrinsic.EncodingInstance {
    return Extrinsic.EncodingInstance(
        signature,
        Extrinsic.EncodingInstance.CallRepresentation.Bytes(callBytes)
    )
}
