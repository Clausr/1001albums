package dk.clausr.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.AlbumImageDao
import dk.clausr.core.database.dao.AlbumWithOptionalRatingDao
import dk.clausr.core.database.dao.LogDao
import dk.clausr.core.database.dao.NotificationDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.RatingDao
import dk.clausr.core.database.model.AlbumEntity
import dk.clausr.core.database.model.AlbumImageEntity
import dk.clausr.core.database.model.LogEntity
import dk.clausr.core.database.model.NotificationEntity
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.database.model.RatingEntity
import dk.clausr.core.database.utils.Converters
import dk.clausr.core.database.utils.DatabaseMigrations

@Database(
    entities = [
        ProjectEntity::class,
        AlbumEntity::class,
        RatingEntity::class,
        AlbumImageEntity::class,
        NotificationEntity::class,
        LogEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15, DatabaseMigrations.Schema14To15::class),
    ],
    version = 17,
)
@TypeConverters(
    Converters::class,
)
abstract class OagDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun albumDao(): AlbumDao
    abstract fun ratingDao(): RatingDao
    abstract fun albumImageDao(): AlbumImageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun logDao(): LogDao
    abstract fun albumWithOptionalRatingDao(): AlbumWithOptionalRatingDao
}
