package dk.clausr.core.model

data class HistoricAlbum(
    val album: Album,
    val rating: Rating,
    val review: String,
    val generatedAt: String, // TODO LocalDateTime
    val globalRating: Double,
)

sealed class Rating {
    data class Rated(val rating: Int) : Rating()
    data object DidNotListen : Rating()
}
