package dk.clausr.core.data.repository

import dk.clausr.core.model.Group
import kotlinx.coroutines.flow.Flow

interface OagRepository {
    fun getGroup(groupId: String): Flow<Group>
}
