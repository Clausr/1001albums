package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.datastore.OagDataStore
import dk.clausr.core.data.model.OagUserData
import kotlinx.coroutines.flow.first
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

    /**
     * Whether to nudge the album cover, to show that the user can swipe between albums. Counts the nudge as shown.
     */
    suspend fun consumeAlbumSwipeHint(): Boolean {
        val shownCount = preferenceDataStore.albumSwipeHintCount.first()
        if (shownCount >= ALBUM_SWIPE_HINT_LIMIT) return false

        preferenceDataStore.setAlbumSwipeHintCount(shownCount + 1)
        return true
    }

    /** The user has found out they can swipe between albums, so there's no need for more nudges. */
    suspend fun setHasSwipedAlbums() = preferenceDataStore.setAlbumSwipeHintCount(ALBUM_SWIPE_HINT_LIMIT)

    private companion object {
        const val ALBUM_SWIPE_HINT_LIMIT = 5
    }
}
