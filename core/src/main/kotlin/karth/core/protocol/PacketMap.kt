package karth.core.protocol

import com.github.michaelbull.logging.InlineLogger
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private typealias PacketStructuresByClass = MutableMap<KClass<out Packet>, PacketStructure<*>>
private typealias PacketStructuresByName = MutableMap<String, PacketStructure<*>>

class ClassPacketStructureMap(
    val structures: PacketStructuresByClass = mutableMapOf()
) : Map<KClass<out Packet>, PacketStructure<*>> by structures {

    inline fun <reified T : ServerPacket> register(
        name: String = T::class.simpleName?:throw Exception("Could not infer name from ${T::class}, try to specify explicitly"),
        init: PacketBuilder<T>.() -> Unit = {}
    ) {
        val builder = PacketBuilder<T>(name).apply(init)
        val structure = builder.build(T::class)
        if (structures.containsKey(T::class)) {
            error("Server packet type already has a structure (packet=${T::class.simpleName}).")
        }
        structures[T::class] = structure

        logger.debug {
            "Register server packet structure (packet=${T::class.simpleName}, " +
                    "opcode=${structure.name})"
        }
    }
    @Suppress("UNCHECKED_CAST")
    operator fun <T : ServerPacket> get(packet: T) =
        structures[packet::class] as? PacketStructure<T>

    fun forHeaderId(headerId: Int) = structures.values.find { it.headerId == headerId }
    fun forName(name: String) = structures.values.find { it.name == name }

    companion object {
        val logger = InlineLogger()
    }
}

class NamePacketStructureMap(
    val structures: PacketStructuresByName = mutableMapOf()
) : Map<String, PacketStructure<*>> by structures {

    inline fun <reified T : ClientPacket> register(
        name: String = T::class.simpleName?:throw Exception("Could not infer name from ${T::class}, try to specify explicitly"),
        init: PacketBuilder<T>.() -> Unit = {
            read {
                T::class.objectInstance
                    ?:T::class.createInstance()
            }
            write {
                // empty packet
            }
        }
    ) {
        val builder = PacketBuilder<T>(name).apply(init)
        val structure = builder.build(T::class)

        if (structures.containsKey(structure.name))
            error("Client packet opcode already has a structure (opcode=${structure.name}).")

        structures[structure.name] = structure

        logger.debug {
            val packet = T::class.simpleName
            "Register client packet structure " +
                    "(packet=$packet, names=${structure.name})"
        }
    }

    companion object {
        val logger = InlineLogger()
    }
}
