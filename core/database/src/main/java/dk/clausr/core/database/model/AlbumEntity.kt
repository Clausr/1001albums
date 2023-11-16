package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dk.clausr.core.model.Album

@Entity(
    tableName = "albums",
)
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
)

fun AlbumEntity.asExternalModel(): Album = Album(
    artist = artist,
    artistOrigin = artist,
    images = emptyList(),
    genres = emptyList(),
    subGenres = emptyList(),
    name = name,
    slug = slug,
    releaseDate = releaseDate,
    globalReviewsUrl = globalReviewsUrl,
    wikipediaUrl = wikipediaUrl,
    spotifyId = spotifyId,
    appleMusicId = appleMusicId,
    tidalId = tidalId,
    amazonMusicId = amazonMusicId,
    youtubeMusicId = youtubeMusicId,
)
