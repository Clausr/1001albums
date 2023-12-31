package dk.clausr.core.database

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.WidgetDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
    @Provides
    fun providesProjectDao(
        database: OagDatabase
    ): ProjectDao = database.projectDao()

    @Provides
    fun providesAlbumDao(
        database: OagDatabase
    ): AlbumDao = database.albumDao()

    @Provides
    fun providesWidgetDao(
        database: OagDatabase
    ): WidgetDao = database.widgetDao()
}
