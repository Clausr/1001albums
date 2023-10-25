package dk.clausr.core.data.repository

import dk.clausr.core.model.Group
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.Flow

interface OagRepository {
    val projectId: Flow<String?>
    val groupId: Flow<String?>
    fun getGroup(groupId: String): Flow<Group>
    fun getProject(projectId: String): Flow<Project>
}
