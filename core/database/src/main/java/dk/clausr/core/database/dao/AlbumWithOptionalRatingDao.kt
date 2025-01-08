package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dk.clausr.core.database.model.AlbumWithOptionalRating
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumWithOptionalRatingDao {

    @Transaction
    @Query(
        """
        SELECT *
        FROM albums
        LEFT JOIN ratings ON albums.slug = ratings.albumSlug
        ORDER BY generatedAt DESC
    """,
    )
    fun getAlbumsWithRatings(): Flow<List<AlbumWithOptionalRating>>

    @Transaction
    @Query(
        """
            SELECT * 
            FROM albums
            LEFT JOIN ratings ON albums.slug = ratings.albumSlug
            WHERE ratings.albumSlug IS NULL  -- Assuming DidNotListen is represented by a lack of rating
        """,
    )
    fun getDidNotListenAlbums(): Flow<List<AlbumWithOptionalRating>>

    @Query(
        """
        SELECT *
        FROM albums
        LEFT JOIN ratings ON albums.slug = ratings.albumSlug
        WHERE slug = (:slug)
        ORDER BY generatedAt DESC
        """,
    )
    suspend fun getAlbumBySlug(slug: String): AlbumWithOptionalRating?

    @Query(
        """
        SELECT *
        FROM albums
        LEFT JOIN ratings ON albums.slug = ratings.albumSlug
        WHERE slug = (:slug)
        ORDER BY generatedAt DESC
        """,
    )
    fun getAlbumBySlugFlow(slug: String): Flow<AlbumWithOptionalRating>

    @Query(
        """
        SELECT * 
        FROM albums
        LEFT JOIN ratings ON albums.slug = ratings.albumSlug
        WHERE ratings.isRevealed = 1
        ORDER BY generatedAt DESC
        LIMIT 1
    """,
    )
    suspend fun getLatestRevealedAlbum(): AlbumWithOptionalRating?

    @Transaction
    @Query(
        """
        SELECT *
        FROM albums
        LEFT JOIN ratings ON albums.slug = ratings.albumSlug
        WHERE LOWER(artist) LIKE LOWER(:artist)   -- Use LIKE for case-insensitive comparison
        ORDER BY releaseDate
    """,
    )
    suspend fun getSimilarAlbumsWithRatings(artist: String): List<AlbumWithOptionalRating>
}
