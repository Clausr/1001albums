package dk.clausr.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.database.utils.DatabaseMigrations
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesOagDatabase(@ApplicationContext context: Context): OagDatabase = Room
        .databaseBuilder(context, OagDatabase::class.java, "oag_database")
        .addMigrations(DatabaseMigrations.MIGRATION_15_TO_16)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
