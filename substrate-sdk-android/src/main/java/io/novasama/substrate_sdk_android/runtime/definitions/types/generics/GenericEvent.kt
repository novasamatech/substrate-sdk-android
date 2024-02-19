package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import io.novasama.substrate_sdk_android.runtime.metadata.eventOrNull
import io.novasama.substrate_sdk_android.runtime.metadata.fullNameOf
import io.novasama.substrate_sdk_android.runtime.metadata.module.Event
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.moduleOrNull
import io.novasama.substrate_sdk_android.scale.dataType.tuple
import io.novasama.substrate_sdk_android.scale.dataType.uint8

@OptIn(ExperimentalUnsignedTypes::class)
object GenericEvent : Type<GenericEvent.Instance>("GenericEvent") {

    class Instance(val module: Module, val event: Event, val arguments: List<Any?>)

    private val indexCoder = tuple(uint8, uint8)

    override val isFullyResolved = true

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Instance {
        val (moduleIndex, eventIndex) = indexCoder.read(scaleCodecReader)
            .run { first.toInt() to second.toInt() }

        val (module, event) = getEventOrThrow(runtime, moduleIndex, eventIndex)

        val arguments = event.arguments.map { argumentDefinition ->
            argumentDefinition.requireNonNull(module, event)
                .decode(scaleCodecReader, runtime)
        }

        return Instance(module, event, arguments)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Instance
    ) = with(value) {
        val (moduleIndex, eventIndex) = event.index

        indexCoder.write(scaleCodecWriter, moduleIndex.toUByte() to eventIndex.toUByte())

        event.arguments.forEachIndexed { index, argumentType ->
            argumentType.requireNonNull(module, event)
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

private fun Type<*>?.requireNonNull(module: Module, event: Event) = this
    ?: throw EncodeDecodeException(
        "Not resolved type in event ${module.fullNameOf(event)}"
    )
