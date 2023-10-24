package dk.clausr.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data.repository.OfflineFirstGroupRepository

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindsOagRepository(
        repo: OfflineFirstGroupRepository
    ): OagRepository
}
