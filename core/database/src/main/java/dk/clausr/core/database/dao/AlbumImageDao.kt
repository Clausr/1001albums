package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dk.clausr.core.database.model.AlbumImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<AlbumImageEntity>)

    @Query(
        """
        SELECT DISTINCT ai.url
        FROM album_images ai
        INNER JOIN ratings r ON ai.albumSlug = r.albumSlug
        WHERE r.rating != "did-not-listen" AND r.rating > 3 AND ai.height > 200 AND ai.height < 400
        ORDER BY r.rating DESC
    """,
    )
    fun getAlbumCovers(): Flow<List<String>>
}
