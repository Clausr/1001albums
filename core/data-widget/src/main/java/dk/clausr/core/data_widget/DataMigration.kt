package dk.clausr.core.data_widget

import androidx.datastore.core.DataMigration

val dataMigration = object : DataMigration<SerializedWidgetState> {
    override suspend fun cleanUp() { /* Intentionally blank */
    }

    override suspend fun shouldMigrate(currentData: SerializedWidgetState): Boolean {
        // Decide if migration is needed (e.g., old data doesn't have `unreadNotifications`)
        return when (currentData) {
            is SerializedWidgetState.Success -> {
                currentData.data.unreadNotifications == null
            }

            else -> false
        }
    }

    override suspend fun migrate(currentData: SerializedWidgetState): SerializedWidgetState {
        return when (currentData) {
            is SerializedWidgetState.Success -> {
                currentData.copy(data = currentData.data.copy(unreadNotifications = 0))
            }

            else -> currentData // No migration needed for other states
        }
    }
}
