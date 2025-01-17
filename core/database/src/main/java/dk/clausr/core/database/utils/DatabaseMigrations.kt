package dk.clausr.core.database.utils

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseMigrations {
    @DeleteTable(tableName = "widget")
    class Schema14To15 : AutoMigrationSpec

    val MIGRATION_15_TO_16 = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE albums ADD COLUMN id TEXT NOT NULL DEFAULT uuid")
        }
    }
}
