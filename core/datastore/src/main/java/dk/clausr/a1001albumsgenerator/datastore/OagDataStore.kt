package dk.clausr.a1001albumsgenerator.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.oagDataStore: DataStore<Preferences> by preferencesDataStore(name = OagDataStore.OAG_DATASTORE_NAME)

class OagDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val hasOnboarded: Flow<Boolean> = context.oagDataStore.data
        .map { preferences ->
            preferences[IS_ONBOARDED] == true
        }

    suspend fun setHasOnboarded(value: Boolean) {
        context.oagDataStore.edit { preferences ->
            preferences[IS_ONBOARDED] = value
        }
    }

    companion object {
        private val IS_ONBOARDED = booleanPreferencesKey("isOnboarded")
        const val OAG_DATASTORE_NAME = "oagConfig"
    }
}
