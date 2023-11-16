package dk.clausr.core.data.repository

import dk.clausr.core.model.Album
import dk.clausr.core.model.OAGWidget
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.Flow

interface OagRepository {
    val projectId: Flow<String?>
    val project: Flow<Project?>
    val widget: Flow<OAGWidget?>
    val albums: Flow<List<Album>>

    suspend fun setProject(projectId: String)
    suspend fun getProject(projectId: String): Project?
    suspend fun getWidget(projectId: String): OAGWidget?
    suspend fun updateDailyAlbum(projectId: String)
}
