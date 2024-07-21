package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.datastore.OagDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val preferenceDataStore: OagDataStore,
) {
    val hasOnboarded: Flow<Boolean> = preferenceDataStore.hasOnboarded

    suspend fun setOnboardingCompleted() {
        preferenceDataStore.setHasOnboarded(true)
    }
}
