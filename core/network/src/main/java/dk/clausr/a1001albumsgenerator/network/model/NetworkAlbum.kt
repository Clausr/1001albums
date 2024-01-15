package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class NetworkAlbum(
    val artist: String,
    val artistOrigin: String? = null,
    val images: List<NetworkAlbumImage>,
    val genres: List<String>,
    val subGenres: List<String>,
    val name: String,
    val slug: String,
    val releaseDate: String,
    val globalReviewsUrl: String,
    val wikipediaUrl: String,
    val spotifyId: String? = null,
    val appleMusicId: String? = null,
    val tidalId: Int? = null,
    val amazonMusicId: String? = null,
    val youtubeMusicId: String? = null,

    @Contextual val generatedAt: Instant? = null,
    @Contextual val listenedAt: Instant? = null,

    //Available for group albums
    val votes: Int? = null,
    val totalRating: Int? = null,
    val averageRating: Double? = null,

    //Available for project albums
    val rating: String? = null,
    val review: String? = null,
    val globalRating: Double? = null,
) {
    @Serializable
    data class NetworkAlbumImage(
        val width: Int,
        val height: Int,
        val url: String,
    )
}
