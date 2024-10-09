package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import dk.clausr.core.database.model.LogEntity

@Dao
interface LogDao {
    @Insert
    suspend fun insertLog(log: LogEntity)
}