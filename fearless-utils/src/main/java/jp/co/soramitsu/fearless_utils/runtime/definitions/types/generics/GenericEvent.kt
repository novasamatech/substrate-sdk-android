package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.runtime.metadata.Event
import jp.co.soramitsu.fearless_utils.runtime.metadata.MetadataType
import jp.co.soramitsu.fearless_utils.runtime.metadata.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.eventOrNull
import jp.co.soramitsu.fearless_utils.runtime.metadata.fullNameOf
import jp.co.soramitsu.fearless_utils.runtime.metadata.moduleOrNull
import jp.co.soramitsu.fearless_utils.scale.dataType.tuple
import jp.co.soramitsu.fearless_utils.scale.dataType.uint8

object GenericEvent : Type<GenericEvent.Instance>("GenericEvent") {

    class Instance(val moduleIndex: Int, val eventIndex: Int, val arguments: List<Any?>)

    private val indexCoder = tuple(uint8, uint8)

    override val isFullyResolved = true

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Instance {
        val (moduleIndex, eventIndex) = indexCoder.read(scaleCodecReader)
            .run { first.toInt() to second.toInt() }

        val (module, event) = getEventOrThrow(runtime, moduleIndex, eventIndex)

        val arguments = event.arguments.map { argumentDefinition ->
            argumentDefinition.requireDerivedType(module, event)
                .decode(scaleCodecReader, runtime)
        }

        return Instance(moduleIndex, eventIndex, arguments)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Instance
    ) = with(value) {
        val (module, event) = getEventOrThrow(runtime, moduleIndex, eventIndex)

        indexCoder.write(scaleCodecWriter, moduleIndex.toUByte() to eventIndex.toUByte())

        event.arguments.forEachIndexed { index, argumentType ->
            argumentType.requireDerivedType(module, event)
                .encodeUnsafe(scaleCodecWriter, runtime, arguments[index])
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Instance
    }

    private fun getEventOrThrow(
        runtime: RuntimeSnapshot,
        moduleIndex: Int,
        eventIndex: Int
    ): Pair<Module, Event> {
        val module = runtime.metadata.moduleOrNull(moduleIndex)
            ?: eventNotFound(moduleIndex, eventIndex)

        val event = module.eventOrNull(eventIndex)
            ?: eventNotFound(moduleIndex, eventIndex)

        return module to event
    }

    private fun eventNotFound(moduleIndex: Int, eventIndex: Int): Nothing {
        throw EncodeDecodeException("No event for ($moduleIndex, $eventIndex) index found")
    }
}

private fun MetadataType.requireDerivedType(module: Module, event: Event) = derivedType
        ?: throw EncodeDecodeException(
            "Type $definition is not resolved in event ${module.fullNameOf(event)}"
        )