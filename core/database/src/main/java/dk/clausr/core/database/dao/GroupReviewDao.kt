package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dk.clausr.core.database.model.GroupReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reviews: List<GroupReviewEntity>)

    @Query("SELECT * FROM group_reviews WHERE albumId = :albumId")
    suspend fun getReviewsFor(albumId: String): List<GroupReviewEntity>

    @Query("SELECT * FROM group_reviews WHERE albumId = :albumId")
    fun getReviewsForFlow(albumId: String): Flow<List<GroupReviewEntity>>
}
