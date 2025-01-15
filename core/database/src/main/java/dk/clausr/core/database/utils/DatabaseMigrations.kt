package dk.clausr.core.database.utils

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

internal object DatabaseMigrations {
    @DeleteTable(tableName = "widget")
    class Schema14To15 : AutoMigrationSpec
}
