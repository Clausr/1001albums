package dk.clausr.core.data.repository

import dk.clausr.core.model.Group
import dk.clausr.core.model.OAGWidget
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.Flow

interface OagRepository {
    val projectId: Flow<String?>
    val groupId: Flow<String?>
    fun getGroup(groupId: String): Flow<Group?>
    fun getProjectFlow(projectId: String): Flow<Project?>
    suspend fun setProject(projectId: String)
    suspend fun getProject(projectId: String): Project?
    fun getWidgetFlow(projectId: String): Flow<OAGWidget?>
    suspend fun getWidget(projectId: String): OAGWidget?

    suspend fun updateDailyAlbum(projectId: String)
}
