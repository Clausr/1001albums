package dk.clausr.core.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val oagDispatcher: OagDispatchers)

enum class OagDispatchers {
    IO,
}
