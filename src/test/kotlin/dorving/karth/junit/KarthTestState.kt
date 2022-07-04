package dorving.karth.junit

import dorving.karth.KarthSession
import gearth.extensions.Extension
import kotlin.reflect.KClass

class KarthTestState(extension: Extension, session: KarthSession) {

    var stubCreators = mutableMapOf<KClass<*>, StubPrototype<Any>.() -> Any>()

    init {
        registerStub<KarthSession> { session }
        registerStub<Extension> { extension }
    }

    fun reset() {

    }

    inline fun<reified T : Any> registerStub(noinline initializer: StubPrototype<T>.() -> T) {
        stubCreators[T::class] = initializer as StubPrototype<Any>.() -> Any
    }

    fun <T : Any> createStub(proto: StubPrototype<T>): T {
        val initializer = stubCreators[proto.type]
        if (initializer == null)
            throw IllegalArgumentException("Can't stub ${proto.type.qualifiedName}")
        else {
            return initializer(proto as StubPrototype<Any>) as T
        }
    }
}
