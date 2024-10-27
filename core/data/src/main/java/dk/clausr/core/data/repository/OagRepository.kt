package dk.clausr.core.data.repository

import dk.clausr.core.common.model.Result
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.core.ui.CoverData
import kotlinx.coroutines.flow.Flow

interface OagRepository {
    val projectId: Flow<String?>
    val project: Flow<Project?>
    val historicAlbums: Flow<List<HistoricAlbum>>
    val currentAlbum: Flow<Album?>

    val widgetState: Flow<SerializedWidgetState>
    val preferredStreamingPlatform: Flow<StreamingPlatform>

    val albumCovers: Flow<CoverData>

    suspend fun setProject(projectId: String): Result<Project, NetworkError>
    suspend fun updateProject(projectId: String): Result<Project, NetworkError>
    suspend fun setPreferredPlatform(platform: StreamingPlatform)
    suspend fun isLatestAlbumRated(): Boolean
    fun getHistoricAlbum(slug: String): Flow<HistoricAlbum>
    suspend fun getSimilarAlbums(artist: String): List<HistoricAlbum>
}
