package dk.clausr.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.WidgetDao
import dk.clausr.core.database.model.AlbumEntity
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.database.model.WidgetEntity
import dk.clausr.core.database.utils.Converters

@Database(
        entities = [
            ProjectEntity::class,
            AlbumEntity::class,
            WidgetEntity::class,
        ],
        version = 3,
        exportSchema = false
)
@TypeConverters(
    Converters::class
)
abstract class OagDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun albumDao(): AlbumDao

    abstract fun widgetDao(): WidgetDao
}
