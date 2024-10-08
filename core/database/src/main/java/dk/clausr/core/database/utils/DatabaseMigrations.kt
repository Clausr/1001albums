package dk.clausr.core.database.utils

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

internal object DatabaseMigrations {
    @DeleteTable(
        tableName = "widget"
    )
    class Schema13To14 : AutoMigrationSpec
}