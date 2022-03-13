package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.SignedExtension
import jp.co.soramitsu.fearless_utils.signing.MultiSignature

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

fun Extrinsic.Companion.create(customSignedExtensions: Collection<SignedExtension>): Extrinsic {
    val customSignedExtensionTypes = customSignedExtensions.associateBy(
        keySelector = { it.name },
        valueTransform = { it.type }
    )

    val allSignedExtensions = SignedExtras.default.extras + customSignedExtensionTypes

    return Extrinsic(ExtrinsicPayloadExtras(allSignedExtensions))
}


fun Extrinsic.Signature.tryExtractMultiSignature(): MultiSignature? {
    val enumEntry = signature as? DictEnum.Entry<*> ?: return null
    val value = enumEntry.value as? ByteArray ?: return null

    return MultiSignature(enumEntry.name, value)
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
