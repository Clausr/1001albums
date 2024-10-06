package dk.clausr.core.common.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers.IO
import dk.clausr.core.common.network.OagDispatchers.Main
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Dispatcher(IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(Main)
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
