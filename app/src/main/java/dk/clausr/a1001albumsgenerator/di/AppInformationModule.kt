package dk.clausr.a1001albumsgenerator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.common.network.AppInformation
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppInformationModule {
    @Singleton
    @Provides
    fun provideAppInformation(): AppInformation = AndroidAppInformation()
}
