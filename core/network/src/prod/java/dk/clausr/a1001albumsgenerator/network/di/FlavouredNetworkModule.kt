package dk.clausr.a1001albumsgenerator.network.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.a1001albumsgenerator.network.NotificationsDataSource
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.retrofit.NotificationsRetrofitDataSource
import dk.clausr.a1001albumsgenerator.network.retrofit.OAGRetrofitDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class FlavouredNetworkModule {
    @Binds
    abstract fun bindOAGDataSource(prod: OAGRetrofitDataSource): OAGDataSource

    @Binds
    abstract fun bindNotificationDataSource(prod: NotificationsRetrofitDataSource): NotificationsDataSource
}
