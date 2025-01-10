package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dk.clausr.core.database.model.RatingEntity

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRatings(ratings: List<RatingEntity>)

    @Query("DELETE FROM ratings")
    suspend fun clearTable()
}
