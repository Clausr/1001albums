package dk.clausr.core.common.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoroutineScopeModule {
    @Provides
    @Singleton
    @ApplicationCoroutineScope
    fun provideApplicationCoroutineScope(@Dispatcher(OagDispatchers.Main) mainDispatcher: CoroutineDispatcher): CoroutineScope {
        return CoroutineScope(SupervisorJob() + mainDispatcher)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationCoroutineScope
