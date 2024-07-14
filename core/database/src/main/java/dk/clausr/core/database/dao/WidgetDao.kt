package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dk.clausr.core.database.model.WidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: WidgetEntity)

    @Update
    suspend fun update(album: WidgetEntity)

    @Delete
    suspend fun delete(album: WidgetEntity)

    @Query("SELECT * FROM widget WHERE projectName = :projectName")
    fun getWidgetFlow(projectName: String): Flow<WidgetEntity?>

    @Query("SELECT * FROM widget WHERE projectName = :projectName")
    suspend fun getWidget(projectName: String): WidgetEntity?

    // Additional queries as needed
}
