package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dk.clausr.core.database.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Upsert
    suspend fun insertProject(project: ProjectEntity)

    @Query(value = "SELECT * FROM project")
    fun getProject(): Flow<ProjectEntity?>
}
