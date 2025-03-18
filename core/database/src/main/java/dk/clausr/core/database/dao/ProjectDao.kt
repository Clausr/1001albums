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

    @Query(value = "SELECT groupSlug FROM project")
    suspend fun getGroupId(): String?

    @Query(value = "SELECT currentAlbumSlug FROM project LIMIT 1")
    suspend fun getCurrentAlbumSlug(): String?

    @Query("DELETE FROM project")
    suspend fun clearTable()
}
