package dk.clausr.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data.repository.OfflineFirstOagRepository
import dk.clausr.core.data_widget.AlbumWidgetDataDefinition
import dk.clausr.core.data_widget.SerializedWidgetState
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindsOagRepository(
        repo: OfflineFirstOagRepository
    ): OagRepository

//    @Binds
//    @Named("simplified")
//    fun bindsSimplifiedDataDefinition(
//        dataDefinition: AlbumWidgetDataDefinition
//    ): GlanceStateDefinition<SerializedWidgetState>
}

@Module
@InstallIn(SingletonComponent::class)
object DataWidgetModule {
    @Provides
    @Singleton
    fun provideStateDefinition(
        @ApplicationContext context: Context,
    ): DataStore<SerializedWidgetState> = runBlocking {
        AlbumWidgetDataDefinition.getDataStore(context)
    }
}
