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
            WHERE (ratings.rating is NULL OR ratings.rating is "did-not-listen")
            AND ratings.isRevealed IS NOT NULL -- Check that no other metadata is saved; e.g. this is the current album
            ORDER BY generatedAt DESC
        """,
    )
    fun getDidNotListenAlbums(): Flow<List<AlbumWithOptionalRating>>

    @Transaction
    @Query(
        """
            SELECT * 
            FROM albums
            LEFT JOIN ratings ON albums.slug = ratings.albumSlug
            WHERE ratings.rating is 5
            ORDER BY generatedAt DESC
        """,
    )
    fun getTopRatedAlbums(): Flow<List<AlbumWithOptionalRating>>

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
        ORDER BY releaseDate DESC
    """,
    )
    suspend fun getSimilarAlbumsWithRatings(artist: String): List<AlbumWithOptionalRating>

    @Transaction
    @Query(
        """
            SELECT a.*, r.*
            FROM albums a
            LEFT JOIN ratings r ON a.slug = r.albumSlug
            WHERE a.slug = :slug
        """
    )
    suspend fun getAlbumWithSlug(slug: String): AlbumWithOptionalRating
}
