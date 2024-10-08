package dk.clausr.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.AlbumImageDao
import dk.clausr.core.database.dao.NotificationDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.RatingDao
import dk.clausr.core.database.dao.WidgetDao
import dk.clausr.core.database.model.AlbumEntity
import dk.clausr.core.database.model.AlbumImageEntity
import dk.clausr.core.database.model.NotificationEntity
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.database.model.RatingEntity
import dk.clausr.core.database.model.WidgetEntity
import dk.clausr.core.database.utils.Converters

@Database(
    entities = [
        ProjectEntity::class,
        AlbumEntity::class,
        WidgetEntity::class,
        RatingEntity::class,
        AlbumImageEntity::class,
        NotificationEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 12, to = 13),
    ],
    version = 13,
)
@TypeConverters(
    Converters::class,
)
abstract class OagDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun albumDao(): AlbumDao
    abstract fun widgetDao(): WidgetDao
    abstract fun ratingDao(): RatingDao
    abstract fun albumImageDao(): AlbumImageDao
    abstract fun notificationDao(): NotificationDao
}
