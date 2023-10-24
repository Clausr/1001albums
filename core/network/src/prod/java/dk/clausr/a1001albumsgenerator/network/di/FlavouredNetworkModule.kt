package dk.clausr.a1001albumsgenerator.network.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.retrofit.DemoDataSource
import dk.clausr.a1001albumsgenerator.network.retrofit.OAGNetworkDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class FlavouredNetworkModule {
    @Binds
    abstract fun bindOAGDataSource(prod: OAGNetworkDataSource): OAGDataSource
}
