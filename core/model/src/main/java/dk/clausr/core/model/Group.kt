package dk.clausr.core.model


data class Group(
    val name: String,
    val slug: String,
    val updateFrequency: UpdateFrequency,
    val filteredSelection: FilteredSelection,
    val currentAlbum: Album,
    val latestAlbum: Album,
    val highestRatedAlbums: List<Album>,
    val lowestRatedAlbums: List<Album>,
    val favoriteGenres: List<Genre>,
    val worstGenres: List<Genre>,
    val ratingByDecade: List<Decade>,
    val numberOfGeneratedAlbums: Int,
    val totalVotes: Int,
) {
    enum class UpdateFrequency {
        DailyWithWeekends,
        DailyWithoutWeekends,
    }
}


data class FilteredSelection(val selections: List<String>, val genres: List<String>)


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


data class Genre(
    val numberOfAlbums: Int,
    val totalRating: Int,
    val votes: Int,
    val genre: String,
    val rating: Double,
    val numberOfVotes: Int,
)

data class Decade(
    val totalRating: Int,
    val votes: Int,
    val numberOfAlbums: Int,
    val decade: String,
    val rating: Double,
)
