package dorving.karth.junit

import dorving.karth.KarthSession
import io.mockk.spyk
import org.junit.jupiter.api.extension.*
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

open class KarthTestingExtension :
    AfterTestExecutionCallback,
    BeforeAllCallback,
    AfterAllCallback,
    BeforeEachCallback,
    AfterEachCallback,
    ParameterResolver {

    private fun cleanup(context: ExtensionContext) {
        val store = context.getStore(namespace)
        val state = store[KarthTestState::class] as KarthTestState
        state.reset()
    }

    override fun afterTestExecution(context: ExtensionContext) =
        cleanup(context)

    override fun beforeAll(context: ExtensionContext) {
        val extension = spyk<TestExtension>()
//        every {
//            extension.modifyMessage()
//        } answers {
//            val packet = firstArg<HPacket>()
//            true
//        }
        val session = KarthSession(extension)
        val store = context.getStore(namespace)
        val state = KarthTestState(extension, session)
        store.put(KarthTestState::class, state)
    }

    override fun afterAll(context: ExtensionContext) {
        val store = context.getStore(namespace)
        store.remove(KarthTestState::class)
    }

    override fun beforeEach(context: ExtensionContext) {
        val testClassInstance = context.requiredTestInstance
        val testClassProps = context.requiredTestClass.kotlin.declaredMemberProperties
        val store = context.getStore(namespace)
        val state = store.get(KarthTestState::class) as KarthTestState
        val propertyStubSites = testClassProps.asSequence()
            .mapNotNull { it as? KMutableProperty<*> }
            .filter { supportedTestTypes.contains(it.returnType) }
        propertyStubSites.forEach { property ->
            property.setter.call(
                testClassInstance,
                state.createStub(StubPrototype(property.returnType.jvmErasure, property.annotations))
            )
        }
    }

    override fun afterEach(context: ExtensionContext) =
        cleanup(context)

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        false

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val param = parameterContext.parameter
        val paramType = param.type.kotlin
        val testStore = extensionContext.getStore(namespace)
        val testState = testStore.get(KarthTestState::class) as KarthTestState

        return testState.createStub(StubPrototype(paramType, param.annotations.toList()))
    }

    companion object {

        val namespace = ExtensionContext.Namespace.create("karth")
        val supportedTestTypes = mutableSetOf(
            KarthSession::class.createType(),
            gearth.extensions.Extension::class.createType(),
        )

        inline fun <reified D : Any, reified A : Annotation> findTestDefinitions(
            callables: Collection<KCallable<*>>,
            companionObjectInstance: Any?
        ): List<D> {
            return callables
                .filter { method -> method.annotations.any { it is A } }
                .flatMap { method ->
                    @Suppress("UNCHECKED_CAST")
                    method as? KCallable<Collection<D>> ?: throw RuntimeException("${method.name} is annotated with " +
                            "${A::class.simpleName} but does not return Collection<${D::class.simpleName}>."
                    )

                    method.isAccessible = true // lets us call methods in private companion objects
                    method.call(companionObjectInstance)
                }
        }
    }
}
