package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    val id: String,
    @PrimaryKey
    val slug: String,
    val artist: String,
    val artistOrigin: String?,
    val name: String,
    val releaseDate: String,
    val globalReviewsUrl: String,
    val wikipediaUrl: String,
    val imageUrl: String,
    val spotifyId: String?,
    val appleMusicId: String?,
    val tidalId: Int?,
    val amazonMusicId: String?,
    val youtubeMusicId: String?,
    val qobuzId: String?,
    val deezerId: String?,
)
