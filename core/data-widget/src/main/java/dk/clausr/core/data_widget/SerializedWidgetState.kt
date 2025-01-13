package dk.clausr.core.data_widget

import dk.clausr.core.common.ExternalLinks
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.StreamingPlatform
import kotlinx.serialization.Serializable

@Serializable
sealed interface SerializedWidgetState {
    @Serializable
    data class Loading(val currentProjectId: String, val previousStreamingPlatform: StreamingPlatform) : SerializedWidgetState

    @Serializable
    data class Success(val data: AlbumWidgetData, val currentProjectId: String) :
        SerializedWidgetState

    @Serializable
    data class Error(val message: String, val currentProjectId: String) :
        SerializedWidgetState

    @Serializable
    data object NotInitialized : SerializedWidgetState

    companion object {
        val SerializedWidgetState.projectId: String?
            get() = when (this) {
                is Loading -> currentProjectId
                is Success -> currentProjectId
                is Error -> currentProjectId
                NotInitialized -> null
            }

        val SerializedWidgetState.projectUrl: String?
            get() = when (this) {
                is Error -> "${ExternalLinks.Generator.BASE_URL}/$projectId"
                is Loading -> "${ExternalLinks.Generator.BASE_URL}/$projectId"
                is Success -> "${ExternalLinks.Generator.BASE_URL}/$projectId"
                NotInitialized -> null
            }
    }
}
