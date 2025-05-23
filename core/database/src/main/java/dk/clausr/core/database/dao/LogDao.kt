package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dk.clausr.core.database.model.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insertLog(log: LogEntity)

    @Query("SELECT COUNT(*) FROM logs")
    fun getRowCount(): Int

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Query("DELETE FROM logs WHERE rowid IN (SELECT rowid FROM logs ORDER BY timestamp ASC LIMIT 1)")
    suspend fun deleteOldestEntry()

    @Query("DELETE FROM logs WHERE rowid IN (SELECT rowid FROM logs ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldestLogs(count: Int)
}
