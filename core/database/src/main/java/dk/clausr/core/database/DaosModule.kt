package dk.clausr.core.database

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.AlbumImageDao
import dk.clausr.core.database.dao.AlbumWithOptionalRatingDao
import dk.clausr.core.database.dao.LogDao
import dk.clausr.core.database.dao.NotificationDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.RatingDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
    @Provides
    fun providesProjectDao(database: OagDatabase): ProjectDao = database.projectDao()

    @Provides
    fun providesAlbumDao(database: OagDatabase): AlbumDao = database.albumDao()

    @Provides
    fun providesRatingDao(database: OagDatabase): RatingDao = database.ratingDao()

    @Provides
    fun providesAlbumImagesDao(database: OagDatabase): AlbumImageDao = database.albumImageDao()

    @Provides
    fun providesNotificationDao(database: OagDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun providesLogDao(database: OagDatabase): LogDao = database.logDao()

    @Provides
    fun providesAlbumWithOptionalRating(database: OagDatabase): AlbumWithOptionalRatingDao = database.albumWithOptionalRatingDao()
}
