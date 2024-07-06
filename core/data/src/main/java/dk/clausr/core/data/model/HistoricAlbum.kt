package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkHistoricAlbum
import dk.clausr.core.database.model.RatingEntity
import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Rating

fun NetworkHistoricAlbum.asExternalModel(): HistoricAlbum = HistoricAlbum(
    album = album.asExternalModel(),
    rating = rating.mapToRating(),
    review = review,
    generatedAt = generatedAt,
    globalRating = globalRating,
    isRevealed = isRevealed,
)

fun NetworkHistoricAlbum.toRatingEntity(): RatingEntity =
    RatingEntity(
        albumSlug = album.slug,
        rating = rating,
        review = review,
        generatedAt = generatedAt,
        globalRating = globalRating,
        isRevealed = isRevealed,
    )

fun RatingEntity.toHistoricAlbum(album: Album): HistoricAlbum = HistoricAlbum(
    album = album,
    rating = rating.mapToRating(),
    review = review,
    generatedAt = generatedAt,
    globalRating = globalRating,
    isRevealed = isRevealed,
)

private fun String?.mapToRating(): Rating = when (this) {
    "did-not-listen" -> Rating.DidNotListen
    null -> Rating.Unrated
    else -> Rating.Rated(this.toInt())
}
