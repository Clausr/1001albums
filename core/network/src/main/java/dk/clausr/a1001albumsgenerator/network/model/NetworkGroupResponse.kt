package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkGroupResponse(
    val name: String,
    val slug: String,
    val updateFrequency: NetworkUpdateFrequency,
    val filteredSelection: NetworkFilteredSelection,
    val currentAlbum: NetworkAlbum,
    val latestAlbum: NetworkAlbum,
    val highestRatedAlbums: List<NetworkAlbum>,
    val lowestRatedAlbums: List<NetworkAlbum>,
    val favoriteGenres: List<NetworkGenre>,
    val worstGenres: List<NetworkGenre>,
    val ratingByDecade: List<NetworkDecade>,
    val numberOfGeneratedAlbums: Int,
    val totalVotes: Int,
) {
    @Serializable
    enum class NetworkUpdateFrequency {
        @SerialName("dailyWithWeekends")
        DailyWithWeekends,

        @SerialName("dailyWithoutWeekends")
        DailyWithoutWeekends,
    }
}


@Serializable
data class NetworkFilteredSelection(val selections: List<String>, val genres: List<String>)

@Serializable
data class NetworkAlbum(
    val artist: String,
    val artistOrigin: String,
    val images: List<NetworkAlbumImage>,
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
    @Serializable
    data class NetworkAlbumImage(
        val width: Int,
        val height: Int,
        val url: String,
    )
}

@Serializable
data class NetworkGenre(
    val numberOfAlbums: Int,
    val totalRating: Int,
    val votes: Int,
    val genre: String,
    val rating: Double,
    val numberOfVotes: Int,
)

@Serializable
data class NetworkDecade(
    val totalRating: Int,
    val votes: Int,
    val numberOfAlbums: Int,
    val decade: String,
    val rating: Double,
)
