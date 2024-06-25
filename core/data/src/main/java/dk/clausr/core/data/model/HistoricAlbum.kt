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
    globalRating = globalRating
)

fun NetworkHistoricAlbum.toRatingEntity(): RatingEntity = RatingEntity(
    albumSlug = album.slug,
    rating = rating ?: "did-not-listen",
    review = review,
    generatedAt = generatedAt,
    globalRating = globalRating,
)


fun RatingEntity.toHistoricAlbum(album: Album): HistoricAlbum = HistoricAlbum(
    album = album,
    rating = rating.mapToRating(),
    review = review,
    generatedAt = generatedAt,
    globalRating = globalRating,
)

private fun String?.mapToRating(): Rating = when (this) {
    null -> Rating.Unrated
    "did-not-listen" -> Rating.DidNotListen
    else -> Rating.Rated(this.toInt())
}
