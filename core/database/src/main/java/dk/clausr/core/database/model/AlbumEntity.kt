package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val slug: String,
    val artist: String,
    val artistOrigin: String,
    val name: String,
    val releaseDate: String,
    val globalReviewsUrl: String,
    val wikipediaUrl: String,
    val spotifyId: String?,
    val appleMusicId: String?,
    val tidalId: Int?,
    val amazonMusicId: String?,
    val youtubeMusicId: String?,
    val votes: Int? = null,
    val totalRating: Int? = null,
    val averageRating: Double? = null,
    val listenedAt: String? = null // TODO: Change to DateTime
)

//fun AlbumEntity.asExternalModel(): Album = Album(
//    artist, artistOrigin, images =
//)
