package dk.clausr.core.model

import java.time.Instant

data class HistoricAlbum(
    val album: Album,
    val metadata: Metadata?,
)

data class Metadata(
    val rating: Rating,
    val review: String,
    val generatedAt: Instant,
    val globalRating: Double,
    val isRevealed: Boolean,
)

sealed class Rating {
    data class Rated(val rating: Int) : Rating()
    data object DidNotListen : Rating()
    data object Unrated : Rating()
}
