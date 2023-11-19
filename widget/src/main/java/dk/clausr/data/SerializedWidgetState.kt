package dk.clausr.data

import dk.clausr.core.model.AlbumWidgetData
import kotlinx.serialization.Serializable

@Serializable
sealed class SerializedWidgetState(val projectId: String? = null) {
    @Serializable
    data class Loading(val currentProjectId: String?) : SerializedWidgetState(currentProjectId)

    @Serializable
    data class Success(val data: AlbumWidgetData, val currentProjectId: String?) :
        SerializedWidgetState(currentProjectId)

    @Serializable
    data class Error(val message: String, val currentProjectId: String?) :
        SerializedWidgetState(currentProjectId)

    @Serializable
    data object NotInitialized : SerializedWidgetState()
}
