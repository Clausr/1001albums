package dk.clausr.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class OagDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val PROJECT_ID = stringPreferencesKey("PROJECT_ID")
    private val GROUP_ID = stringPreferencesKey("GROUP_ID")
    private val WIDGET_MAP = stringSetPreferencesKey("WIDGET_MAP")

    val projectId: Flow<String?> = context.dataStore.data.map { preferences ->
        val projectId = preferences[PROJECT_ID]
        Timber.d("projectId from preferences: $projectId")
        projectId
    }.distinctUntilChanged()

    val groupId: Flow<String?> = context.dataStore.data.map { preferences ->
        val groupId = preferences[GROUP_ID]
        Timber.d("groupId from preferences: $groupId")
        groupId
    }.distinctUntilChanged()

    suspend fun setProject(newProjectId: String) {
        context.dataStore.edit { preferences ->
            preferences[PROJECT_ID] = newProjectId
        }
    }

    suspend fun setGroup(newGroupId: String) {
        Timber.d("setGroup $newGroupId")
        context.dataStore.edit { preferences ->
            preferences[GROUP_ID] = newGroupId
        }
    }

//    suspend fun setProjectForWidget(widgetId: Int, projectId: String) {
//        context.dataStore.edit { preferences ->
//
//        }
//    }

}
