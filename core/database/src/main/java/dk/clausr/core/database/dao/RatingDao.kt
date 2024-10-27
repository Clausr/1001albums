package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dk.clausr.core.database.model.RatingEntity
import dk.clausr.core.database.model.RatingWithAlbum
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRatings(ratings: List<RatingEntity>)

    @Query("SELECT * FROM ratings WHERE albumSlug = :albumSlug")
    suspend fun getRatingByAlbumSlug(albumSlug: String): RatingEntity?

    @Transaction
    @Query("SELECT * FROM ratings ORDER BY generatedAt DESC")
    fun getRatingsWithAlbums(): Flow<List<RatingWithAlbum>>

    @Transaction
    @Query("SELECT * FROM ratings WHERE albumSlug = :slug")
    fun getRatingWithAlbum(slug: String): Flow<RatingWithAlbum>

    @Transaction
    @Query("SELECT * FROM ratings WHERE albumSlug IN (:slugs)")
    fun getAlbumRatings(slugs: List<String>): List<RatingWithAlbum>

    @Query("DELETE FROM ratings")
    suspend fun clearTable()
}
