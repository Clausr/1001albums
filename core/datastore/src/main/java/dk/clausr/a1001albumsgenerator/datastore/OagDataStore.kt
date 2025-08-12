package dk.clausr.a1001albumsgenerator.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.oagDataStore: DataStore<Preferences> by preferencesDataStore(name = OagDataStore.OAG_DATASTORE_NAME)

class OagDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val userData: Flow<UserData> = context.oagDataStore.data.map { preferences ->
        UserData(
            hasOnboarded = preferences[IS_ONBOARDED] == true,
            projectId = preferences[PROJECT_ID],
            groupSlug = preferences[GROUP_SLUG],
        )
    }

    suspend fun setHasOnboarded(value: Boolean) {
        context.oagDataStore.edit { preferences ->
            preferences[IS_ONBOARDED] = value
        }
    }

    suspend fun setProjectId(id: String?) {
        context.oagDataStore.edit {
            if (id?.isNotBlank() == true) {
                it[PROJECT_ID] = id
            } else {
                it.remove(PROJECT_ID)
            }
        }
    }

    suspend fun setGroupSlug(slug: String?) {
        context.oagDataStore.edit {
            if (slug?.isNotBlank() == true) {
                it[GROUP_SLUG] = slug
            } else {
                it.remove(GROUP_SLUG)
            }
        }
    }

    companion object {
        private val PROJECT_ID = stringPreferencesKey("project_id")
        private val GROUP_SLUG = stringPreferencesKey("group_slug")
        private val IS_ONBOARDED = booleanPreferencesKey("isOnboarded")
        const val OAG_DATASTORE_NAME = "oagConfig"
    }
}
