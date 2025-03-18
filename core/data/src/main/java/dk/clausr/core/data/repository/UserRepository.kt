package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.datastore.OagDataStore
import dk.clausr.core.data.model.OagUserData
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val preferenceDataStore: OagDataStore,
) {
    val userData = preferenceDataStore.userData.map {
        OagUserData(
            hasOnboarded = it.hasOnboarded,
            projectId = it.projectId,
            groupSlug = it.groupSlug,
        )
    }

    suspend fun setOnboardingCompleted() = preferenceDataStore.setHasOnboarded(true)

    suspend fun setProjectId(id: String) = preferenceDataStore.setProjectId(id)
}
