package dk.clausr.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OagDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val projectIdKey = stringPreferencesKey("PROJECT_ID")

    val projectId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[projectIdKey]
    }.distinctUntilChanged()

    suspend fun setProjectId(newProjectId: String) = context.dataStore.edit { preferences ->
        preferences[projectIdKey] = newProjectId
    }
}
