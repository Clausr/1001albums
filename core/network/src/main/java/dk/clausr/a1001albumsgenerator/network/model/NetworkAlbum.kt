package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkAlbum(
    @SerialName("uuid")
    val id: String,
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
    val qobuzId: String? = null,
    val deezerId: String? = null,
) {
    @Serializable
    data class NetworkAlbumImage(
        val width: Int,
        val height: Int,
        val url: String,
    )
}
