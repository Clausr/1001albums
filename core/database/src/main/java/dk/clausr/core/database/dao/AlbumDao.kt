package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dk.clausr.core.database.model.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Update
    suspend fun update(album: AlbumEntity)

    @Delete
    suspend fun delete(album: AlbumEntity)

    @Query("DELETE FROM albums")
    suspend fun clearTable()

    @Query("SELECT * FROM albums WHERE slug = :albumId")
    suspend fun getAlbumBySlug(albumId: String): AlbumEntity?

    // Additional queries as needed
    @Query("SELECT * FROM albums")
    fun getAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT slug from albums WHERE LOWER(artist) LIKE LOWER(:originalArtist)")
    fun getSimilarAlbumSlugs(originalArtist: String): List<String>
}
