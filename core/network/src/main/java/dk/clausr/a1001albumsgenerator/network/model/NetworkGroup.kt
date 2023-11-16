package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkGroup(
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
)

@Serializable
enum class NetworkUpdateFrequency {
    @SerialName("dailyWithWeekends")
    DailyWithWeekends,

    @SerialName("daily")
    DailyWithoutWeekends,
}


@Serializable
data class NetworkFilteredSelection(val selections: List<String>, val genres: List<String>)


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
