package dk.clausr.a1001albumsgenerator.network.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.a1001albumsgenerator.network.NotificationsDataSource
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.retrofit.notifications.NotificationsMockDataSource
import dk.clausr.a1001albumsgenerator.network.retrofit.oag.OAGMockDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class FlavouredNetworkModule {
    @Binds
    abstract fun bindOAGDataSource(mock: OAGMockDataSource): OAGDataSource

    @Binds
    abstract fun bindNotificationDataSource(mock: NotificationsMockDataSource): NotificationsDataSource
}
