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
)

data class FilteredSelection(val selections: List<String>, val genres: List<String>)

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
