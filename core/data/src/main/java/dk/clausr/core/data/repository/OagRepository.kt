package dk.clausr.core.data.repository

import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.Flow

interface OagRepository {
    val projectId: Flow<String?>
    val project: Flow<Project?>
    val historicAlbums: Flow<List<HistoricAlbum>>

//    val projectWithAlbums: Flow<ProjectWithAlbums?>

    val widgetState: Flow<SerializedWidgetState>

    suspend fun setProject(projectId: String)
    suspend fun updateProject()
}
