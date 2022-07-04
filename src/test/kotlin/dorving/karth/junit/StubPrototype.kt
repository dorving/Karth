package dorving.karth.junit

import kotlin.reflect.KClass

data class StubPrototype<T : Any>(
    val type: KClass<T>,
    val annotations: Collection<Annotation>,
)
