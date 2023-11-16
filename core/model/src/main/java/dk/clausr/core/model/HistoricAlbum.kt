package dk.clausr.core.model

import java.time.Instant

data class HistoricAlbum(
    val album: Album,
    val rating: Rating,
    val review: String,
    val generatedAt: Instant,
    val globalRating: Double,
)

sealed class Rating {
    data class Rated(val rating: Int) : Rating()
    data object DidNotListen : Rating()
    data object Unrated : Rating()
}
