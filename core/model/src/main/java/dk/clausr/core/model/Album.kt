package dk.clausr.core.model

data class Album(
    val artist: String,
    val artistOrigin: String,
    val images: List<AlbumImage>,
    val genres: List<String>,
    val subGenres: List<String>,
    val name: String,
    val slug: String,
    val releaseDate: String,
    val globalReviewsUrl: String,
    val wikipediaUrl: String,
    val spotifyId: String,
    val appleMusicId: String,
    val tidalId: Int,
    val amazonMusicId: String,
    val youtubeMusicId: String,
    val votes: Int? = null,
    val totalRating: Int? = null,
    val averageRating: Double? = null,
    val listenedAt: String? = null, //TOOD DateTime
) {
    data class AlbumImage(
        val width: Int,
        val height: Int,
        val url: String,
    )
}
