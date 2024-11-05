package dk.clausr.a1001albumsgenerator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.a1001albumsgenerator.utils.RoomLoggingTree
import dk.clausr.core.data.repository.LoggingRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideRoomLoggingTree(loggingRepository: LoggingRepository): RoomLoggingTree {
        return RoomLoggingTree(loggingRepository)
    }
}
